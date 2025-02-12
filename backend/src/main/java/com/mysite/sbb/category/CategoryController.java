package com.mysite.sbb.category;

import com.mysite.sbb.global.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@RestController
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseDto<List<CategoryDto>> getAllCategories() {
        return new ResponseDto<>(
                categoryService
                        .getAllCategories()
                        .stream()
                        .map(CategoryDto::new)
                        .collect(Collectors.toList())
        );
    }
}
