package com.example.kubernetestesst.service;


import com.example.kubernetestesst.entities.Book;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IBookService {
    void createBook(Book book);
    List<Book> getAllBooks();
    Book getBookById(Integer bookId);
}
