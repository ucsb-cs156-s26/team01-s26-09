package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDiningCommonsMenuItems */
@Tag(name = "UCSBDiningCommonsMenuItems")
@RequestMapping("/api/ucsbdiningcommonsmenuitems")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemsController extends ApiController {
  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSBDiningCommonsMenuItems
   *
   * @return an iterable of UCSBDiningCommonsMenuItems
   */
  @Operation(summary = "List all UCSB Dining Commons Menu Items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> menuItems = ucsbDiningCommonsMenuItemRepository.findAll();
    return menuItems;
  }

  /**
   * Get a single UCSN Dining Commons Menu Item by id
   *
   * @param id the id of the date
   * @return a UCSB Dining Commons Menu Item
   */
  @Operation(summary = "Get a single UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem ucsbMenuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    return ucsbMenuItem;
  }

  /**
   * Create a new menu item
   *
   * @param diningCommonsCode the dining commons code
   * @param name the name of the menu item
   * @param station the station of the menu item
   * @return the saved ucsbdiningcommonsmenuitem
   */
  @Operation(summary = "Create a new ucsb dining commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    log.info("diningCommonsCode={}", diningCommonsCode);
    log.info("name={}", name);
    log.info("station={}", station);

    UCSBDiningCommonsMenuItem ucsbMenuItem = new UCSBDiningCommonsMenuItem();
    ucsbMenuItem.setDiningCommonsCode(diningCommonsCode);
    ucsbMenuItem.setName(name);
    ucsbMenuItem.setStation(station);

    UCSBDiningCommonsMenuItem savedUcsbMenuItem =
        ucsbDiningCommonsMenuItemRepository.save(ucsbMenuItem);

    return savedUcsbMenuItem;
  }

  /**
   * Update a single UCSB Dining Commons Menu Item
   *
   * @param id id of the UCSB Dining Commons Menu Item to update
   * @param incoming the new UCSB Dining Commons Menu Item
   * @return the updated UCSB Dining Commons Menu Item
   */
  @Operation(summary = "Update a single UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem ucsbMenuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbMenuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    ucsbMenuItem.setName(incoming.getName());
    ucsbMenuItem.setStation(incoming.getStation());

    ucsbDiningCommonsMenuItemRepository.save(ucsbMenuItem);

    return ucsbMenuItem;
  }

  /**
   * Delete a UCSBDining commons menu items
   *
   * @param id the id of the menu item to delete
   * @return a message indicating the menu item was deleted
   */
  @Operation(summary = "Delete a UCSBDining commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDiningCommonsMenuItem(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem ucsbMenuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItemRepository.delete(ucsbMenuItem);
    return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
  }
}
