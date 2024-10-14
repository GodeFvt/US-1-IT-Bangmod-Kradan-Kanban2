package sit.us1.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sit.us1.backend.dtos.tasksDTO.*;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.TaskList;
import sit.us1.backend.entities.taskboard.TaskStatus;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.repositories.taskboard.TaskLimitRepository;
import sit.us1.backend.repositories.taskboard.TaskListRepository;
import sit.us1.backend.repositories.taskboard.TaskStatusRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskListRepository taskRepository;
    @Autowired
    private TaskStatusRepository statusRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper mapper;

    public boolean isTaskExist(Integer taskId) {
        return taskRepository.existsById(taskId);
    }
    public List<SimpleTaskDTO> getAllTask() {
        return listMapper.mapList(taskRepository.findAll(), SimpleTaskDTO.class, mapper);
    }

    public List<SimpleTaskDTO> getTaskFiltered(String sortBy, String[] filterStatuses ,String boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        try {
            Sort sort = Sort.by(sortBy == null || sortBy.isEmpty() ? "createdOn" : sortBy);
            if (filterStatuses.length > 0) {
                return listMapper.mapList(taskRepository.findByStatusNamesSorted(filterStatuses,boardId, sort), SimpleTaskDTO.class, mapper);
            } else {
                return listMapper.mapList(taskRepository.findAllByBoardId(boardId,sort), SimpleTaskDTO.class, mapper);
            }
        } catch (Exception e) {
            throw new BadRequestException("Invalid sortBy property: " + sortBy);
        }
    }

    public TaskDetailDTO getTaskById(String boardId ,Integer id) {
        TaskList task = taskRepository.findByBoardIdAndId(boardId,id).orElseThrow(() -> new NotFoundException("the specified task does not exist"));
        return mapper.map(task, TaskDetailDTO.class);
    }

    public StatusCountDTO getCountByStatusIdAndReturnStatusName(String boardId ,Integer statusId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        TaskStatus status ;
        if(board.getIsCustomStatus()){
            status = statusRepository.findByBoardIdAndId(boardId,statusId).orElseThrow(() -> new NotFoundException("Status Id not found: " + statusId));
        }else {
            status = statusRepository.findById(statusId).orElseThrow(() -> new NotFoundException("Status Id not found: " + statusId));
        }
        StatusCountDTO StatusCount = taskRepository.countByStatusIdAndReturnName(boardId,statusId);
        return StatusCount == null ? new StatusCountDTO(0L, status.getName()) : StatusCount;
    }

    @Transactional
    public TaskResponseDTO createTask(String boardId, TaskRequestDTO taskRequestDTO) {
        TaskList task = mapper.map(taskRequestDTO, TaskList.class);
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        TaskStatus status;
        if (board.getIsCustomStatus()) {
            status = statusRepository.findByBoardIdAndName(boardId, taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        } else {
            status = statusRepository.findByNameAndBoardIdIsNull(taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        }
        try {
            task.setStatus(status);
            task.setBoard(board);
            TaskList newTask = taskRepository.save(task);
            return mapper.map(newTask, TaskResponseDTO.class);
        } catch (Exception e) {
            throw new NotFoundException("Failed to add This task");
        }
    }

    @Transactional
    public TaskResponseDTO updateTask(String boardId, Integer taskId, TaskRequestDTO taskRequestDTO) {
        TaskList taskList = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("the specified task does not exist"));
        TaskStatus status;
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        if (board.getIsCustomStatus()) {
            status = statusRepository.findByBoardIdAndName(boardId, taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        } else {
            status = statusRepository.findByNameAndBoardIdIsNull(taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        }
        try {
            taskRequestDTO.setId(taskId);
            TaskList task = mapper.map(taskRequestDTO, TaskList.class);
            task.setStatus(status);
            task.setBoard(board);
            TaskList newTask = taskRepository.save(task);
            return mapper.map(newTask, TaskResponseDTO.class);
        } catch (Exception e) {
            throw new NotFoundException("Failed to update this task with Id number :" + taskId);
        }
    }

    @Transactional
    public SimpleTaskDTO deleteTask(String boardId , Integer taskId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        TaskList taskList = taskRepository.findByBoardIdAndId(boardId,taskId).orElseThrow(() -> new NotFoundException("the specified task does not exist"));
        taskRepository.delete(taskList);
        return mapper.map(taskList, SimpleTaskDTO.class);
    }


}





