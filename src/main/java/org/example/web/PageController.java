package org.example.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring-based controller that maps GET requests to HTML files.
 *
 * @author Dominik Grzelak
 */
@Controller
public class PageController {

    @RequestMapping(value = "/index")
    public String index() {
        return "index";
    }
}