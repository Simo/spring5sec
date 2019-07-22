package it.insiel.gcs.prove.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @RequestMapping("/")
    public ModelAndView index() {

        ModelAndView mv = new ModelAndView();
        mv.setViewName("index");
        mv.addObject("name", "Simone");

        return mv;
    }

    @RequestMapping("hello")
    public ModelAndView hello() {
        return new ModelAndView("hello");
    }

}
