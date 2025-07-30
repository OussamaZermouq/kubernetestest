package com.example.kubernetestesst.controller;

import com.example.kubernetestesst.entities.Book;
import com.example.kubernetestesst.service.IBookService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/book")
public class BookController {

    @Autowired
    private IBookService bookService;

    @GetMapping("/")
    private ResponseEntity<List<Book>> getAllBooks(){
        return ResponseEntity.ok().body(bookService.getAllBooks());
    }

    @PostMapping("/create")
    private ResponseEntity<Map<Integer, String>> createBook(@RequestBody Book book){
        bookService.createBook(book);
        return ResponseEntity.ok().body(Map.of(200, "Created successfully"));
    }

    @GetMapping("/{bookId}")
    private ResponseEntity<?> getBookById(@PathVariable Integer bookId){
        return ResponseEntity.ok().body(bookService.getBookById(bookId));
    }
}
