package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class BookRepository implements ProjectRepository<Book>, ApplicationContextAware {

    private final Logger logger = Logger.getLogger(BookRepository.class);
    ApplicationContext context;

    private NamedParameterJdbcTemplate jdbcTemplate;

    public BookRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Book> retrieveAll() {
        return jdbcTemplate.query("select * from books", (ResultSet rs, int index) -> {
            Book book = new Book();
            book.setId(rs.getInt("id"));
            book.setAuthor(rs.getString("author"));
            book.setTitle(rs.getString("title"));
            book.setSize(rs.getInt("size"));
            return book;
        });
    }

    @Override
    public void store(Book item) {
        SqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("author", item.getAuthor())
                .addValue("title", item.getTitle())
                .addValue("size", item.getSize());
        jdbcTemplate.update("insert into books(author, title, size) values(:author, :title, :size)", sqlParams);
        logger.info("store new book: " + item);
    }

    @Override
    public boolean removeItemById(Integer itemToRemove) {
        SqlParameterSource sqlParams = new MapSqlParameterSource().addValue("id", itemToRemove);
        int n = jdbcTemplate.update("delete from books where id = :id", sqlParams);
        return n > 0;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
