package org.example.web.controllers;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.example.app.config.FileService;
import org.example.app.services.BookService;
import org.example.web.dto.Book;
import org.example.web.dto.BookIdToRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping(value = "/books")
public class BookShelfController {

    private final Logger logger = Logger.getLogger(BookShelfController.class);
    private final BookService bookService;
    private final CommonsMultipartResolver multipartResolver;
    private final FileService fileService;

    /**
     * Возвращает подготовленный book_shelf
     * в него надо добавить некоторые объекты - они представлены параметрами
     * если в параметр передать null - будет использовано значение по-умолчанию (кроме модели)
     */
    public String prepareBookShelf(Model model,
                                    Book book,
                                    BookIdToRemove toRemove,
                                    Book toRemoveByExpr,
                                    Book toFilterByExpr,
                                    List<Book> bookList) {
        if (book == null) {
            model.addAttribute("book", new Book());
        } else {
            model.addAttribute("book", book);
        }
        if (toRemove == null) {
            model.addAttribute("bookIdToRemove", new BookIdToRemove());
        } else {
            model.addAttribute("bookIdToRemove", toRemove);
        }
        if (toRemoveByExpr == null) {
            model.addAttribute("bookToRemoveByExpr", new Book());
        } else {
            model.addAttribute("bookToRemoveByExpr", toRemoveByExpr);
        }
        if (toFilterByExpr == null) {
            model.addAttribute("bookToFilterByExpr", new Book());
        } else {
            model.addAttribute("bookToFilterByExpr", toFilterByExpr);
        }
        if (bookList == null) {
            model.addAttribute("bookList", bookService.getAllBooks());
        } else {
            model.addAttribute("bookList", bookList);
        }
        return "book_shelf";
    }

    @Autowired
    public BookShelfController(BookService bookService,
                               CommonsMultipartResolver commonsMultipartResolver,
                               FileService fileService) {
        this.bookService = bookService;
        this.multipartResolver = commonsMultipartResolver;
        this.fileService = fileService;
    }

    @GetMapping("/shelf")
    public String books(Model model) {
        logger.info("got book shelf");
        return prepareBookShelf(model, null, null, null, null, null);
    }

    @PostMapping("/save")
    public String saveBook(@Valid Book book,
                           BindingResult bindingResult,
                           Model model,
                           @ModelAttribute("bookIdToRemove") BookIdToRemove bookIdToRemove,
                           @ModelAttribute("bookToRemoveByExpr") Book bookToRemoveByExpr,
                           @ModelAttribute("bookToFilterByExpr") Book bookToFilterByExpr,
                           @ModelAttribute("bookList") ArrayList<Book> bookList) {
        if (bindingResult.hasErrors()) {
            logger.warn("cannot save - errors in book found");
            return prepareBookShelf(model,
                    book,
                    bookIdToRemove,
                    bookToRemoveByExpr,
                    bookToFilterByExpr,
                    bookList);
        } else {
            bookService.saveBook(book);
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/remove")
    public String removeBook(@Valid BookIdToRemove bookIdToRemove,
                             BindingResult bindingResult,
                             Model model,
                             @ModelAttribute("book") Book book,
                             @ModelAttribute("bookToRemoveByExpr") Book bookToRemoveByExpr,
                             @ModelAttribute("bookToFilterByExpr") Book bookToFilterByExpr,
                             @ModelAttribute("bookList") ArrayList<Book> bookList) {
        if (bindingResult.hasErrors()) {
            return prepareBookShelf(model,
                    book,
                    bookIdToRemove,
                    bookToRemoveByExpr,
                    bookToFilterByExpr,
                    bookList);
        } else {
            bookService.removeBookById(bookIdToRemove.getId());
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/remove-by-expr")
    public String removeBooksByExpr(@ModelAttribute("bookToRemoveByExpr") Book book) {
        logger.info("got filter book for remove: " + book);
        // удалим книги по выражению
        bookService.removeBookByFilter(book);
        return "redirect:/books/shelf";
    }
    @PostMapping("/filter-by-expr")
    public String filterBooksByExpr(Model model, @ModelAttribute("bookToFilterByExpr") Book filterBook) {
        logger.info("got filter params: " + filterBook);
        return prepareBookShelf(model,
                (Book) model.getAttribute("book"),
                (BookIdToRemove) model.getAttribute("bookIdToRemove"),
                (Book) model.getAttribute("bookToRemoveByExpr"),
                filterBook,
                bookService.getBooksFiltered(filterBook));
    }

    @PostMapping(value = "/upload-file")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) throws Exception {
        String fileName = file.getOriginalFilename();
        byte[] fileContent = file.getBytes();

        // create dir
        String rootPath = System.getProperty("catalina.home");
        File dir = Paths.get(rootPath, "uploads").toFile();
        if (!dir.exists()) {
            logger.info("create upload directory");
            dir.mkdirs();
        }

        // create file
        try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(Paths.get(dir.getAbsolutePath(), fileName).toString()))) {
            stream.write(fileContent);
        } catch (IOException e) {
            logger.error(e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "errors/404";
        }
        return "redirect:/books/shelf";
    }

    @GetMapping(value = "/files", produces = MediaType.TEXT_HTML_VALUE)
    public String listFiles(Model model) {
        model.addAttribute("fileList", fileService.getFiles());
        return "files_page";
    }

    @PostMapping(value = "/files")
    public void loadFile(@ModelAttribute("filename") String fileName,
                           HttpServletResponse response,
                           Model model ) {
        logger.info("get filename " + fileName);
        fileService.downloadFile(fileName, response);
    }

    @ExceptionHandler({Exception.class})
    public String handleBookShelfExceptions(Model model, Exception e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "errors/404";
    }
}
