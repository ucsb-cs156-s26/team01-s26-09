package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Articles. */
@Tag(name = "Articles")
@RequestMapping("/api/Articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {

  @Autowired ArticlesRepository articlesRepository;

  /**
   * List all articles.
   *
   * @return an iterable of articles
   */
  @Operation(summary = "List all articles")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<Articles> allArticles() {
    return articlesRepository.findAll();
  }

  /**
   * Get a single article by id.
   *
   * @param id id of the article to get
   * @return the requested article
   */
  @Operation(summary = "Get a single article")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public Articles getById(
      @Parameter(name = "id", description = "ID of the article to retrieve") @RequestParam
          Long id) {
    Articles article =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    return article;
  }

  /**
   * Create a new article.
   *
   * @param title article title
   * @param url article URL
   * @param explanation short explanation of the article
   * @param email submitter email
   * @param dateAdded the timestamp when the article was added
   * @return the saved article
   */
  @Operation(summary = "Create a new article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public Articles postArticles(
      @Parameter(name = "title") @RequestParam String title,
      @Parameter(name = "url") @RequestParam String url,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "email") @RequestParam String email,
      @Parameter(
              name = "dateAdded",
              description =
                  "timestamp in ISO format, e.g. YYYY-mm-ddTHH:MM:SS; see"
                      + " https://en.wikipedia.org/wiki/ISO_8601")
          @RequestParam("dateAdded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateAdded) {

    Articles article = new Articles();
    article.setTitle(title);
    article.setUrl(url);
    article.setExplanation(explanation);
    article.setEmail(email);
    article.setDateAdded(dateAdded);

    return articlesRepository.save(article);
  }

  /**
   * Delete an article by id.
   *
   * @param id id of the article to delete
   * @return a message indicating the article was deleted
   */
  @Operation(summary = "Delete a single article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteArticle(
      @Parameter(name = "id", description = "ID of the article to delete") @RequestParam Long id) {
    Articles article =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    articlesRepository.delete(article);
    return genericMessage("Articles with id %s deleted".formatted(id));
  }

  /**
   * Update an existing article by id.
   *
   * @param id id of the article to update
   * @param incoming JSON body with updated article field values
   * @return the updated article
   */
  @Operation(summary = "Update a single article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public Articles updateArticles(
      @Parameter(name = "id", description = "ID of the article to update") @RequestParam Long id,
      @RequestBody @Valid Articles incoming) {

    Articles article =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    article.setTitle(incoming.getTitle());
    article.setUrl(incoming.getUrl());
    article.setExplanation(incoming.getExplanation());
    article.setEmail(incoming.getEmail());
    article.setDateAdded(incoming.getDateAdded());

    articlesRepository.save(article);

    return article;
  }
}
