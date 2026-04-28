package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticlesRepository articlesRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/Articles/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/Articles/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/Articles?id=7")).andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/Articles/post")).andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    mockMvc.perform(put("/api/Articles?id=7")).andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/Articles?id=7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/Articles/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_put() throws Exception {
    LocalDateTime dateAdded = LocalDateTime.parse("2024-05-06T07:08:09");
    Articles article =
        Articles.builder()
            .title("Updated Article")
            .url("https://example.org/updated")
            .explanation("Updated explanation")
            .email("updated@example.org")
            .dateAdded(dateAdded)
            .build();

    String requestBody = mapper.writeValueAsString(article);

    mockMvc
        .perform(
            put("/api/Articles?id=7")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(requestBody)
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/Articles?id=7").with(csrf())).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
    LocalDateTime dateAdded = LocalDateTime.parse("2024-01-02T03:04:05");

    Articles article =
        Articles.builder()
            .title("Requested Article")
            .url("https://example.org/requested")
            .explanation("Requested explanation")
            .email("requested@example.org")
            .dateAdded(dateAdded)
            .build();

    when(articlesRepository.findById(eq(7L))).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc.perform(get("/api/Articles?id=7")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
    when(articlesRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc.perform(get("/api/Articles?id=7")).andExpect(status().isNotFound()).andReturn();

    verify(articlesRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {
    LocalDateTime dateAdded1 = LocalDateTime.parse("2024-01-02T03:04:05");
    LocalDateTime dateAdded2 = LocalDateTime.parse("2024-04-05T06:07:08");

    Articles article1 =
        Articles.builder()
            .title("First Article")
            .url("https://example.org/one")
            .explanation("First explanation")
            .email("first@example.org")
            .dateAdded(dateAdded1)
            .build();

    Articles article2 =
        Articles.builder()
            .title("Second Article")
            .url("https://example.org/two")
            .explanation("Second explanation")
            .email("second@example.org")
            .dateAdded(dateAdded2)
            .build();

    ArrayList<Articles> expectedArticles = new ArrayList<>();
    expectedArticles.addAll(Arrays.asList(article1, article2));

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    MvcResult response =
        mockMvc.perform(get("/api/Articles/all")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    LocalDateTime dateAdded = LocalDateTime.parse("2024-01-02T03:04:05");

    Articles article =
        Articles.builder()
            .title("New Article")
            .url("https://example.org/article")
            .explanation("Useful explanation")
            .email("author@example.org")
            .dateAdded(dateAdded)
            .build();

    when(articlesRepository.save(eq(article))).thenReturn(article);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/Articles/post")
                    .param("title", "New Article")
                    .param("url", "https://example.org/article")
                    .param("explanation", "Useful explanation")
                    .param("email", "author@example.org")
                    .param("dateAdded", "2024-01-02T03:04:05")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).save(eq(article));
    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_article() throws Exception {
    LocalDateTime dateAdded = LocalDateTime.parse("2024-01-02T03:04:05");

    Articles article =
        Articles.builder()
            .id(15L)
            .title("Delete Me")
            .url("https://example.org/delete")
            .explanation("Delete explanation")
            .email("delete@example.org")
            .dateAdded(dateAdded)
            .build();

    when(articlesRepository.findById(eq(15L))).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc
            .perform(delete("/api/Articles?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(15L);
    verify(articlesRepository, times(1)).delete(article);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_article_that_does_not_exist() throws Exception {
    when(articlesRepository.findById(eq(15L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/Articles?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(15L);
    verify(articlesRepository, times(0)).delete(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 15 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_article() throws Exception {
    LocalDateTime originalDateAdded = LocalDateTime.parse("2024-01-02T03:04:05");
    LocalDateTime updatedDateAdded = LocalDateTime.parse("2024-05-06T07:08:09");

    Articles articleOrig =
        Articles.builder()
            .id(67L)
            .title("Original Article")
            .url("https://example.org/original")
            .explanation("Original explanation")
            .email("original@example.org")
            .dateAdded(originalDateAdded)
            .build();

    Articles articleEdited =
        Articles.builder()
            .id(67L)
            .title("Updated Article")
            .url("https://example.org/updated")
            .explanation("Updated explanation")
            .email("updated@example.org")
            .dateAdded(updatedDateAdded)
            .build();

    String requestBody = mapper.writeValueAsString(articleEdited);

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(articleOrig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/Articles?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(67L);
    verify(articlesRepository, times(1)).save(articleEdited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_article_that_does_not_exist() throws Exception {
    LocalDateTime updatedDateAdded = LocalDateTime.parse("2024-05-06T07:08:09");

    Articles articleEdited =
        Articles.builder()
            .title("Updated Article")
            .url("https://example.org/updated")
            .explanation("Updated explanation")
            .email("updated@example.org")
            .dateAdded(updatedDateAdded)
            .build();

    String requestBody = mapper.writeValueAsString(articleEdited);

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/Articles?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(67L);
    verify(articlesRepository, times(0)).save(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 67 not found", json.get("message"));
  }
}
