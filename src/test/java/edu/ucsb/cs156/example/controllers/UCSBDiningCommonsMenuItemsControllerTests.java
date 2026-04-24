package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemsController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemsControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/ucsbdiningcommonsmenuitems/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitems/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitems/all"))
        .andExpect(status().is(200)); // logged
  }

  // @Test
  // public void logged_out_users_cannot_get_by_id() throws Exception {
  //   mockMvc
  //       .perform(get("/api/ucsbdiningcommonsmenuitems?id=7"))
  //       .andExpect(status().is(403)); // logged out users can't get by id
  // }

  // Authorization tests for /api/ucsbdiningcommonsmenuitems/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsbdiningcommonsmenuitems/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsbdiningcommonsmenuitems/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // // Tests with mocks for database actions

  // @WithMockUser(roles = {"USER"})
  // @Test
  // public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

  //   // arrange

  //   UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
  //       UCSBDiningCommonsMenuItem.builder()
  //           .diningCommonsCode("DeC a")
  //           .name("green beans")
  //           .station("main dish")
  //           .build();

  //
  // when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.of(ucsbDiningCommonsMenuItem));

  //   // act
  //   MvcResult response =
  //
  // mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems?id=7")).andExpect(status().isOk()).andReturn();

  //   // assert

  //   verify(ucsbDateRepository, times(1)).findById(eq(7L));
  //   String expectedJson = mapper.writeValueAsString(ucsbDate);
  //   String responseString = response.getResponse().getContentAsString();
  //   assertEquals(expectedJson, responseString);
  // }

  // @WithMockUser(roles = {"USER"})
  // @Test
  // public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws
  // Exception {

  //   // arrange

  //   when(ucsbDateRepository.findById(eq(7L))).thenReturn(Optional.empty());

  //   // act
  //   MvcResult response =
  //
  // mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems?id=7")).andExpect(status().isNotFound()).andReturn();

  //   // assert

  //   verify(ucsbDateRepository, times(1)).findById(eq(7L));
  //   Map<String, Object> json = responseToJson(response);
  //   assertEquals("EntityNotFoundException", json.get("type"));
  //   assertEquals("UCSBDate with id 7 not found", json.get("message"));
  // }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsbdiningcommonsmenuitems() throws Exception {

    // arrange
    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("DLG")
            .name("green beans")
            .station("main dish")
            .build();

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem2 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("DLG")
            .name("chicken tenders")
            .station("main dish")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedMenuItems = new ArrayList<>();
    expectedMenuItems.addAll(Arrays.asList(ucsbDiningCommonsMenuItem1, ucsbDiningCommonsMenuItem2));

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedMenuItems);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitems/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedMenuItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_ucsbdiningcommonsmenuitem() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("DLG")
            .name("green_beans")
            .station("main_dish")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(eq(ucsbDiningCommonsMenuItem1)))
        .thenReturn(ucsbDiningCommonsMenuItem1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitems/post?diningCommonsCode=DLG&name=green_beans&station=main_dish")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(ucsbDiningCommonsMenuItem1);
    String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenuItem1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_can_delete_a_date() throws Exception {
  //   // arrange

  //   LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

  //   UCSBDate ucsbDate1 =
  //       UCSBDate.builder()
  //           .name("firstDayOfClasses")
  //           .quarterYYYYQ("20222")
  //           .localDateTime(ldt1)
  //           .build();

  //   when(ucsbDateRepository.findById(eq(15L))).thenReturn(Optional.of(ucsbDate1));

  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(delete("/api/ucsbdiningcommonsmenuitems?id=15").with(csrf()))
  //           .andExpect(status().isOk())
  //           .andReturn();

  //   // assert
  //   verify(ucsbDateRepository, times(1)).findById(15L);
  //   verify(ucsbDateRepository, times(1)).delete(any());

  //   Map<String, Object> json = responseToJson(response);
  //   assertEquals("UCSBDate with id 15 deleted", json.get("message"));
  // }

  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
  //     throws Exception {
  //   // arrange

  //   when(ucsbDateRepository.findById(eq(15L))).thenReturn(Optional.empty());

  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(delete("/api/ucsbdiningcommonsmenuitems?id=15").with(csrf()))
  //           .andExpect(status().isNotFound())
  //           .andReturn();

  //   // assert
  //   verify(ucsbDateRepository, times(1)).findById(15L);
  //   Map<String, Object> json = responseToJson(response);
  //   assertEquals("UCSBDate with id 15 not found", json.get("message"));
  // // }

  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_can_edit_an_existing_ucsbdate() throws Exception {
  //   // arrange

  //   LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
  //   LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

  //   UCSBDate ucsbDateOrig =
  //       UCSBDate.builder()
  //           .name("firstDayOfClasses")
  //           .quarterYYYYQ("20222")
  //           .localDateTime(ldt1)
  //           .build();

  //   UCSBDate ucsbDateEdited =
  //       UCSBDate.builder()
  //           .name("firstDayOfFestivus")
  //           .quarterYYYYQ("20232")
  //           .localDateTime(ldt2)
  //           .build();

  //   String requestBody = mapper.writeValueAsString(ucsbDateEdited);

  //   when(ucsbDateRepository.findById(eq(67L))).thenReturn(Optional.of(ucsbDateOrig));

  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(
  //               put("/api/ucsbdiningcommonsmenuitems?id=67")
  //                   .contentType(MediaType.APPLICATION_JSON)
  //                   .characterEncoding("utf-8")
  //                   .content(requestBody)
  //                   .with(csrf()))
  //           .andExpect(status().isOk())
  //           .andReturn();

  //   // assert
  //   verify(ucsbDateRepository, times(1)).findById(67L);
  //   verify(ucsbDateRepository, times(1)).save(ucsbDateEdited); // should be saved with correct
  // user
  //   String responseString = response.getResponse().getContentAsString();
  //   assertEquals(requestBody, responseString);
  // }

  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
  //   // arrange

  //   LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

  //   UCSBDate ucsbEditedDate =
  //       UCSBDate.builder()
  //           .name("firstDayOfClasses")
  //           .quarterYYYYQ("20222")
  //           .localDateTime(ldt1)
  //           .build();

  //   String requestBody = mapper.writeValueAsString(ucsbEditedDate);

  //   when(ucsbDateRepository.findById(eq(67L))).thenReturn(Optional.empty());

  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(
  //               put("/api/ucsbdiningcommonsmenuitems?id=67")
  //                   .contentType(MediaType.APPLICATION_JSON)
  //                   .characterEncoding("utf-8")
  //                   .content(requestBody)
  //                   .with(csrf()))
  //           .andExpect(status().isNotFound())
  //           .andReturn();

  //   // assert
  //   verify(ucsbDateRepository, times(1)).findById(67L);
  //   Map<String, Object> json = responseToJson(response);
  //   assertEquals("UCSBDate with id 67 not found", json.get("message"));
  // }
}
