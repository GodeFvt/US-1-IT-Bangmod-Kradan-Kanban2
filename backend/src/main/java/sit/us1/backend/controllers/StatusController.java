package sit.us1.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO;
import sit.us1.backend.entities.TaskLimit;
import sit.us1.backend.services.StatusService;
import sit.us1.backend.validations.ValidStatusId;
import sit.us1.backend.validations.ValidStatusLimit;
import sit.us1.backend.validations.ValidationGroups;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://ip23us1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/statuses")
public class StatusController {
    @Autowired
    private StatusService service;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<SimpleStatusDTO>> getStatusList() {
        return ResponseEntity.ok(service.getAllStatus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleStatusDTO> getStatusById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getStatusById(id));
    }

    @GetMapping("/limit")
    public ResponseEntity<TaskLimit> getStatusLimit() {
        return ResponseEntity.ok(service.getStatusLimit());
    }

    @PostMapping("")
    public ResponseEntity<SimpleStatusDTO> createStatus(@Validated({ValidationGroups.OnCreate.class,Default.class}) @RequestBody SimpleStatusDTO newStatus) {
        SimpleStatusDTO status = service.createStatus(newStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimpleStatusDTO> updateStatus(@ValidStatusId @PathVariable Integer id,  @Validated @RequestBody SimpleStatusDTO statusDTO) {
        SimpleStatusDTO status = service.updateStatus(id, statusDTO);
        return ResponseEntity.ok(status);
    }

    @PatchMapping("/all/maximum-task")
    public ResponseEntity<List<StatusLimitResponseDTO>> updateLimitMaxiMunTask( @RequestParam @Min(0) @Max(30) Integer maximumTask, @RequestParam Boolean isLimit) {
        List<StatusLimitResponseDTO> status = service.updateLimitMaxiMunTask(maximumTask, isLimit);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SimpleStatusDTO> deleteStatus(@ValidStatusId @PathVariable Integer id) {
        SimpleStatusDTO status = service.deleteStatus(id);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/{id}/{newId}")
    public ResponseEntity<SimpleStatusDTO> deleteStatusAndTransferStatusInAllTask(@ValidStatusId @PathVariable Integer id, @ValidStatusLimit @PathVariable Integer newId) {
        SimpleStatusDTO status = service.deleteStatusAndTransferStatusInAllTask(id, newId);
        return ResponseEntity.ok(status);
    }
}
