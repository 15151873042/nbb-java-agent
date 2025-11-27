package com.nbb;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.pool.TypePool.Default;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.lang.instrument.Instrumentation;
import java.util.Map;

/**
 * @author 胡鹏
 */
public class FeignAgent {


    /**
     * JVM 启动时调用（必须是 public static void 类型，参数固定）
     *
     * @param args 启动时传入的 Agent 参数（通过 -javaagent:agent.jar=args 指定）
     * @param inst Instrumentation 核心工具类（JVM 注入）
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("[FeignAgent] Premain 开始，params：" + args);

        // 1、构建类型池（TypePool）；ByteBuddy 提供的「类型池」，用于高效查找、加载目标类的元信息（比如类的结构、方法、字段），避免重复反射操作，提升性能
        TypePool typePool = Default.ofSystemLoader();
        // 2、初始化 ByteBuddy 并指定要增强的类
        new ByteBuddy() // 创建 ByteBuddy 核心操作对象（后续所有字节码增强逻辑都通过它链式调用）
                .redefine( // 指定要「重定义」的目标类（即修改已有类的字节码，而非创建新类）
                        typePool.describe("org.springframework.cloud.openfeign.FeignClientsRegistrar").resolve(), // 通过类型池找到 FeignClientsRegistrar 类的 Class 对象（describe("全类名") 定位，resolve() 解析为 Class）
                        ClassFileLocator.ForClassLoader.ofSystemLoader() // 告诉 ByteBuddy 从系统类加载器中读取 FeignClientsRegistrar 的原始字节码（用于后续修改）
                )
                // 3、匹配要拦截的目标方法
                .method(ElementMatchers
                        .named("getUrl") // 方法名必须是 getUrl（Feign 中获取服务 URL 的核心方法）；
                        .and(ElementMatchers.takesArguments(ConfigurableBeanFactory.class, Map.class)) // 方法的入参为这两个
                        .and(ElementMatchers.isDeclaredBy(typePool.describe("org.springframework.cloud.openfeign.FeignClientsRegistrar").resolve())) // 方法必须是 FeignClientsRegistrar 类自己声明的（排除继承来的同名方法）
                        .and(ElementMatchers.returns(String.class)) // 方法返回值是 String 类型（getUrl 原生返回值就是服务 URL 字符串
                )
                // 4、指定方法的替代实现（拦截逻辑）
                .intercept(MethodDelegation.to(FixedUrlFeignClientsRegistrar.class)) // 将匹配到的 getUrl 方法的实现，委托给 FeignClientLocal 类
                // 5. 生成增强后的字节码并加载
                .make() // 生成增强后的类字节码
                .load(
                        ClassLoader.getSystemClassLoader(), // 用系统类加载器加载增强后的类
                        ClassLoadingStrategy.Default.INJECTION // 加载策略：注入到现有类加载器中
                );

        System.out.println("FeignAgent初始化成功");
    }
}
