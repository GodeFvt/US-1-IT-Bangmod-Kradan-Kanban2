package sit.us1.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sit.us1.backend.dtos.tasksDTO.*;
import sit.us1.backend.entities.taskboard.TaskList;
import sit.us1.backend.entities.taskboard.TaskStatus;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.taskboard.TaskLimitRepository;
import sit.us1.backend.repositories.taskboard.TaskListRepository;
import sit.us1.backend.repositories.taskboard.TaskStatusRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskListRepository repository;
    @Autowired
    private TaskStatusRepository statusRepository;
    @Autowired
    private TaskLimitRepository limitRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper mapper;
    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;

    public List<SimpleTaskDTO> getAllTask() {
        return listMapper.mapList(repository.findAll(), SimpleTaskDTO.class, mapper);
    }

    public List<SimpleTaskDTO> getTaskFiltered(String sortBy, String[] filterStatuses) {
        try {
            Sort sort = Sort.by(sortBy == null || sortBy.isEmpty() ? "createdOn" : sortBy);
            if (filterStatuses.length > 0) {
                return listMapper.mapList(repository.findByStatusNamesSorted(filterStatuses, sort), SimpleTaskDTO.class, mapper);
            } else {
                return listMapper.mapList(repository.findAll(sort), SimpleTaskDTO.class, mapper);
            }
        } catch (Exception e) {
            throw new BadRequestException("Invalid sortBy property: " + sortBy);
        }
    }

    public TaskDetailDTO getTaskById(Integer id) {
        TaskList task = repository.findById(id).orElseThrow(() -> new BadRequestException("the specified task does not exist"));
        return mapper.map(task, TaskDetailDTO.class);
    }

    public StatusCountDTO getCountByStatusIdAndReturnStatusName(Integer statusId) {
        TaskStatus status = statusRepository.findById(statusId).orElseThrow(() -> new BadRequestException("the specified status does not exist"));
        StatusCountDTO StatusCount = repository.countByStatusIdAndReturnName(statusId);
        return StatusCount == null ? new StatusCountDTO(0L, status.getName()) : StatusCount;
    }

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        TaskList task = mapper.map(taskRequestDTO, TaskList.class);
        TaskStatus status = statusRepository.findByName(taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        try {
            task.setStatus(status);
            List<TaskList> TaskList = new ArrayList<>();
            TaskList.add(task);
            status.setTaskList(TaskList);
            statusRepository.save(status);
            TaskList newTask = repository.save(task);
            return mapper.map(newTask, TaskResponseDTO.class);
        } catch (Exception e) {
            throw new NotFoundException("Failed to add This task");
        }

    }

    @Transactional
    public TaskResponseDTO updateTask(Integer taskId, TaskRequestDTO taskRequestDTO) {

        TaskList taskList = repository.findById(taskId).orElseThrow(() -> new BadRequestException("the specified task does not exist"));

        try {
            taskRequestDTO.setId(taskId);
            TaskList task = mapper.map(taskRequestDTO, TaskList.class);
            TaskStatus status = statusRepository.findByName(taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status not found: " + taskRequestDTO.getStatus()));
            task.setStatus(status);
            List<TaskList> TaskList = new ArrayList<>();
            TaskList.add(task);
            status.setTaskList(TaskList);
            TaskList newTask = repository.save(task);
            return mapper.map(newTask, TaskResponseDTO.class);
        } catch (Exception e) {
            throw new NotFoundException("Failed to update this task with Id number :" + taskId);
        }
    }

    @Transactional
    public SimpleTaskDTO deleteTask(Integer taskId) {
        TaskList taskList = repository.findById(taskId).orElseThrow(() -> new BadRequestException("the specified task does not exist"));
        repository.delete(taskList);
        return mapper.map(taskList, SimpleTaskDTO.class);
    }


}





