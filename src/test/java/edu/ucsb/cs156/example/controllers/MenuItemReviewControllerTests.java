package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(200));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menu_item_reviews() throws Exception {
    LocalDateTime reviewedAt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime reviewedAt2 = LocalDateTime.parse("2022-03-11T00:00:00");

    MenuItemReview review1 =
        MenuItemReview.builder()
            .itemId(123L)
            .reviewerEmail("reviewer1@ucsb.edu")
            .stars(5)
            .dateReviewed(reviewedAt1)
            .comments("Excellent")
            .build();

    MenuItemReview review2 =
        MenuItemReview.builder()
            .itemId(456L)
            .reviewerEmail("reviewer2@ucsb.edu")
            .stars(2)
            .dateReviewed(reviewedAt2)
            .comments("Not great")
            .build();

    ArrayList<MenuItemReview> expectedReviews = new ArrayList<>();
    expectedReviews.addAll(Arrays.asList(review1, review2));
    when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedReviews);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemId", "123")
                .param("reviewerEmail", "reviewer1@ucsb.edu")
                .param("stars", "5")
                .param("dateReviewed", "2022-01-03T00:00:00")
                .param("comments", "Excellent")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemId", "123")
                .param("reviewerEmail", "reviewer1@ucsb.edu")
                .param("stars", "5")
                .param("dateReviewed", "2022-01-03T00:00:00")
                .param("comments", "Excellent")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menu_item_review() throws Exception {
    LocalDateTime reviewedAt = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(123L)
            .reviewerEmail("reviewer1@ucsb.edu")
            .stars(5)
            .dateReviewed(reviewedAt)
            .comments("Excellent")
            .build();

    when(menuItemReviewRepository.save(eq(review))).thenReturn(review);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreview/post")
                    .param("itemId", "123")
                    .param("reviewerEmail", "reviewer1@ucsb.edu")
                    .param("stars", "5")
                    .param("dateReviewed", "2022-01-03T00:00:00")
                    .param("comments", "Excellent")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).save(review);
    String expectedJson = mapper.writeValueAsString(review);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }
}
