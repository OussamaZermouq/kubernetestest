package com.example.kubernetestesst.service;

import com.example.kubernetestesst.entities.Book;
import com.example.kubernetestesst.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookImplementation implements IBookService{
    @Autowired
    private BookRepository bookRepository;

    @Override
    public void createBook(Book book) {
        bookRepository.save(book);
    }

    @Override
    public List<Book> getAllBooks() {

        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Integer bookId) {
        return bookRepository.findById(bookId).orElseThrow();
    }
}
