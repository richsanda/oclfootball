package w.whatever.data.jpa.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by rich on 1/29/16.
 */
@Controller
public class OclController {

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

}
