package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.services.BookService;
import org.example.web.dto.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/books")
public class BookShelfController {

    private Logger logger = Logger.getLogger(BookShelfController.class);
    private BookService bookService;

    @Autowired
    public BookShelfController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/shelf")
    public String books(Model model) {
        logger.info("got book shelf");
        model.addAttribute("book", new Book());
        model.addAttribute("bookList", bookService.getAllBooks());
        return "book_shelf";
    }

    @PostMapping("/save")
    public String saveBook(Book book) {
        bookService.saveBook(book);
        return "redirect:/books/shelf";
    }

    @PostMapping("/remove")
    public String removeBook(@RequestParam(value = "bookIdToRemove") String bookIdToRemove) {
        // залипуха, чтобы можно было большие id вводить
        Integer idToRemove = null;
        try {
            idToRemove = Integer.parseInt(bookIdToRemove);
        } catch (NumberFormatException nfe) {
            idToRemove = 0;
        }
        bookService.removeBookById(idToRemove);
        return "redirect:/books/shelf";
    }

    @PostMapping("/remove-by-expr")
    public String removeBooksByExpr(Book book) {
        logger.info("got filter book for remove: " + book);
        // удалим книги по выражению
        bookService.removeBookByFilter(book);
        return "redirect:/books/shelf";
    }
    @PostMapping("/filter-by-expr")
    public String filterBooksByExpr(Model model,
                                    @RequestParam(value = "size", required = false) Integer size,
                                    @RequestParam(value = "author", required = false) String author,
                                    @RequestParam(value = "title", required = false) String title) {
        logger.info("got filter params: size = \"" + size + "\" author = \"" + author + "\" title = "+ title + "\"");
        // объект для фильтрации по регулярке
        Book filterBook = new Book();
        filterBook.setAuthor(author);
        filterBook.setSize(size);
        filterBook.setTitle(title);
        //

        System.out.println(Arrays.toString(bookService.getBooksFiltered(filterBook).toArray()));
        model.addAttribute("modelList", bookService.getBooksFiltered(filterBook));
        model.addAttribute("book", new Book());
        return "book_shelf";
    }
}
