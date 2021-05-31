package com.ex.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpashopConfig{
    @Bean
    Hibernate5Module hibernate5Module(){
        //예제로 하지만, 이건 필요없는 짓 : 그 이유는 엔티티를 외부에 노출 안하고 DTO를 써야하니깐
        //근데 굳이 쓰자면, LAZY 인 FetchType은 무시해버려
        return new Hibernate5Module();
        /**
         * Hibernate5Module hibernate5Module = new Hibernate5Module();
         * hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
         * return hibernate5Module;
         * 의미: LAZY FetchType도 모두 넣어버려
         */
    }
}