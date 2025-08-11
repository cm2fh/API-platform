package com.zyb.backend.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel配置类
 */
@Configuration
@Slf4j
public class SentinelConfig {

    /**
     * 注册Sentinel切面，用于支持@SentinelResource注解
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 初始化限流规则
     */
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 全局限流规则 - QPS不超过100
        FlowRule globalRule = new FlowRule();
        globalRule.setResource("api-gateway-filter");
        globalRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        globalRule.setCount(100); // 每秒最多100个请求
        globalRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        globalRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(globalRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel流控规则初始化完成，共{}条规则", rules.size());
    }

    /**
     * 初始化熔断规则
     */
    @PostConstruct
    public void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 异常比例熔断规则
        DegradeRule exceptionRule = new DegradeRule();
        exceptionRule.setResource("api-gateway-filter");
        exceptionRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        exceptionRule.setCount(0.5); // 异常比例阈值 50%
        exceptionRule.setTimeWindow(10); // 熔断时长 10秒
        exceptionRule.setMinRequestAmount(5); // 最小请求数
        exceptionRule.setStatIntervalMs(1000); // 统计时长 1秒
        rules.add(exceptionRule);

        // 慢调用比例熔断规则
        DegradeRule slowCallRule = new DegradeRule();
        slowCallRule.setResource("api-gateway-filter");
        slowCallRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        slowCallRule.setCount(2000); // 响应时间阈值 2秒
        slowCallRule.setTimeWindow(10); // 熔断时长 10秒
        slowCallRule.setMinRequestAmount(5); // 最小请求数
        slowCallRule.setSlowRatioThreshold(0.5); // 慢调用比例阈值 50%
        slowCallRule.setStatIntervalMs(1000); // 统计时长 1秒
        rules.add(slowCallRule);

        DegradeRuleManager.loadRules(rules);
        log.info("Sentinel熔断规则初始化完成，共{}条规则", rules.size());
    }
}
