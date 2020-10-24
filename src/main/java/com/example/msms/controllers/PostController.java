package com.example.msms.controllers;

import com.example.msms.models.ApiResponse;
import com.example.msms.models.Post;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class PostController {


    private final RestTemplate restTemplate;

    public PostController(RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
    }

    @GetMapping()
    public String postList(Model model) {
        Post[] posts = restTemplate.getForObject("http://localhost:8084/api/posts/all", Post[].class);

        model.addAttribute("post", posts);
        return "postList";
    }

    @GetMapping(path = "/create")
    public String createFrom(Model model) {
        model.addAttribute("post", new Post());

        return "createPost";
    }

    @PostMapping(path = "/create")
    public String createAction(@ModelAttribute Post post, Model model) {
        ApiResponse response = restTemplate.postForObject("http://localhost:8084/api/posts/add", post, ApiResponse.class);

        model.addAttribute("post", new Post());
        model.addAttribute("response", response);

        return "createPost";
    }

    @GetMapping(path = "/update")
    public String updateFrom(@RequestParam() Integer postId, Model model) {
        Post post = restTemplate.getForObject("http://localhost:8084/api/posts/" + postId, Post.class);
        model.addAttribute("post", post);

        return "updatePost";
    }

    @PostMapping(path = "/update")
    public String updateAction(@ModelAttribute Post post, Model model) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonBody = objectMapper.writeValueAsString(post);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ApiResponse response = restTemplate.exchange(
                    "http://localhost:8084/api/posts/" + post.getId(),
                    HttpMethod.PUT,
                    entity,
                    ApiResponse.class
            ).getBody();

            model.addAttribute("post", post);
            model.addAttribute("response", response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            ApiResponse failed = new ApiResponse();
            failed.setStatus(500);
            failed.setMessage("Failed parsing JSON");
            model.addAttribute("response", failed);
            model.addAttribute("post", post);
        }

        return "updatePost";
    }

    @GetMapping(path = "/delete")
    public String deleteFrom(@RequestParam() Integer postId, Model model) {
        Post post = restTemplate.getForObject("http://localhost:8084/api/posts/" + postId, Post.class);
        model.addAttribute("post", post);

        return "deletePost";
    }

    @PostMapping(path = "/delete")
    public String deleteAction(@ModelAttribute Post post, Model model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ApiResponse response = restTemplate.exchange(
                "http://localhost:8084/api/posts/" + post.getId(),
                HttpMethod.DELETE,
                entity,
                ApiResponse.class
        ).getBody();

        model.addAttribute("post", post);
        model.addAttribute("response", response);

        return "deletePost";
    }
}
