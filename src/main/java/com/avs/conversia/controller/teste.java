package com.avs.conversia.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class teste {

   
    @GetMapping
    public String getMethodName() {
        return "Ola deu tudo certo ";
    }

    @GetMapping("/teste")
    public String getTeste() {
        return "Ola deu tudo certo no teste";
    }
    
}
