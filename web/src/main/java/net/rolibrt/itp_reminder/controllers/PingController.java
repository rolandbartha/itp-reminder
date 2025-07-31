package net.rolibrt.itp_reminder.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/ping")
public class PingController {

    @GetMapping
    @ResponseBody
    public String ping() {
        return "pong";
    }
}
