package com.avs.conversia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class teste {

   
    // @GetMapping
    // public String getMethodName() {
    //     return "Ola deu tudo certo ";
    // }

    @GetMapping("/")
    public String getTeste() {
        return "index.html";
    }
    
}
