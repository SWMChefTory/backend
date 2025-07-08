package com.example.mockrecipecreator;

import com.example.mockrecipecreator.dto.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/")
public class RecipeController {

    @PostMapping("/caption")
    public ResponseEntity<ClientCaptionResponse> getCaption(@RequestParam String videoId,
                                                            @RequestParam(required = false) String type) {
        // Mock 응답 생성
        ClientCaptionResponse response = new ClientCaptionResponse();
        response.setLangCode("ko");

        List<Segment> segments = Arrays.asList(
                new Segment(0.0, 5.0, "안녕하세요, 오늘은 맛있는 요리를 만들어보겠습니다."),
                new Segment(5.1, 10.0, "먼저 재료를 준비해주세요."),
                new Segment(10.1, 15.0, "양파를 잘게 다져주세요."),
                new Segment(15.1, 20.0, "팬에 기름을 두르고 양파를 볶아주세요.")
        );
        response.setSegments(segments);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/ingredients")
    public ResponseEntity<ClientIngredientsResponse> getIngredients(@RequestParam String videoId) {
        // Mock 응답 생성 (captionContent를 기반으로 재료 추출했다고 가정)
        System.out.println("VideoId: " + videoId);
        System.out.println("Caption Content: " );

        ClientIngredientsResponse response = new ClientIngredientsResponse();

        // captionContent에 따라 다른 재료를 반환할 수도 있음
        List<Ingredient> ingredients = Arrays.asList(
                new Ingredient("양파", 1, "개"),
                new Ingredient("당근", 2, "개"),
                new Ingredient("감자", 3, "개"),
                new Ingredient("쇠고기", 500, "g"),
                new Ingredient("간장", 2, "큰술"),
                new Ingredient("설탕", 1, "작은술")
        );
        response.setIngredients(ingredients);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/step")
    public ResponseEntity<ClientStepsResponse> getSteps(@RequestParam String videoId,
                                                        @RequestBody String segments) {
        // Mock 응답 생성 (segments를 기반으로 단계 추출했다고 가정)
        System.out.println("VideoId: " + videoId);
        System.out.println("!!!!!!!!!!!!!!!!!");
        System.out.println("Segments: " + segments);

        ClientStepsResponse response = new ClientStepsResponse();

        // segments에 따라 다른 단계를 반환할 수도 있음
        List<ClientStepResponse> steps = Arrays.asList(
                new ClientStepResponse("재료 준비하기",
                        Arrays.asList("양파 1개를 준비합니다", "당근 2개를 준비합니다", "감자 3개를 준비합니다", "쇠고기 500g을 준비합니다"),
                        0.0, 30.0),
                new ClientStepResponse("채소 손질하기",
                        Arrays.asList("양파를 잘게 다집니다", "당근을 한입 크기로 자릅니다", "감자를 한입 크기로 자릅니다"),
                        30.1, 90.0),
                new ClientStepResponse("고기 볶기",
                        Arrays.asList("팬에 기름을 두릅니다", "쇠고기를 넣고 중불에서 볶습니다", "고기가 익을 때까지 볶아줍니다"),
                        90.1, 150.0),
                new ClientStepResponse("채소 볶기",
                        Arrays.asList("볶은 고기에 양파를 넣습니다", "당근과 감자를 추가합니다", "모든 재료를 함께 볶아줍니다"),
                        150.1, 210.0),
                new ClientStepResponse("조림하기",
                        Arrays.asList("간장 2큰술을 넣습니다", "설탕 1작은술을 넣습니다", "약불에서 10분간 조려줍니다"),
                        210.1, 300.0)
        );
        response.setSteps(steps);

        return ResponseEntity.ok(response);
    }
}
