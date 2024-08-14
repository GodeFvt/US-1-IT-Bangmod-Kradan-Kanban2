package sit.us1.backend.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.tasksDTO.*;
import sit.us1.backend.services.TaskService;


import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://ip23us1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/tasks")
public class TasksController {
    @Autowired
    private TaskService service;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<SimpleTaskDTO>> getTaskFiltered(@RequestParam(defaultValue = "") String sortBy,
                                                               @RequestParam(defaultValue = "") String[] filterStatuses) {
        return ResponseEntity.ok(service.getTaskFiltered(sortBy, filterStatuses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDetailDTO> getTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getTaskById(id));
    }

    @GetMapping("/count/status/{statusId}")
    public StatusCountDTO countTasksByStatusId(@PathVariable Integer statusId) {
        return service.getCountByStatusIdAndReturnStatusName(statusId);
    }

    @PostMapping("")
    public ResponseEntity<TaskResponseDTO> createTask(@Validated @RequestBody TaskRequestDTO newTask) {
        TaskResponseDTO taskList = service.createTask(newTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskList);
    }


    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@Validated @RequestBody TaskRequestDTO task, @PathVariable Integer id) {
        TaskResponseDTO taskList = service.updateTask(id, task);
        return ResponseEntity.ok(taskList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SimpleTaskDTO> deleteTask(@Valid @PathVariable Integer id) {
        SimpleTaskDTO taskList = service.deleteTask(id);
        return ResponseEntity.ok(taskList);
    }

}
