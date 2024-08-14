package sit.us1.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.limitsDTO.TaskInLimitDTO;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO;
import sit.us1.backend.dtos.tasksDTO.StatusCountDTO;
import sit.us1.backend.entities.TaskLimit;
import sit.us1.backend.entities.TaskList;
import sit.us1.backend.entities.TaskStatus;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.ValidationException;
import sit.us1.backend.repositories.TaskLimitRepository;
import sit.us1.backend.repositories.TaskListRepository;
import sit.us1.backend.repositories.TaskStatusRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusService {
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

    public List<SimpleStatusDTO> getAllStatus() {
        return listMapper.mapList(statusRepository.findAll(), SimpleStatusDTO.class, mapper);
    }

    public TaskLimit getStatusLimit() {
        return limitRepository.findById(1).orElseThrow(() -> new BadRequestException("Limit not found"));
    }

    public SimpleStatusDTO getStatusById(Integer id) {
        TaskStatus status = statusRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified status does not exist"));
        return mapper.map(status, SimpleStatusDTO.class);
    }
    @Transactional
    public SimpleStatusDTO createStatus(SimpleStatusDTO statusDTO) {
        TaskStatus status = mapper.map(statusDTO, TaskStatus.class);
        try {
            TaskStatus newStatus = statusRepository.save(status);
            return mapper.map(newStatus, SimpleStatusDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to add This status");
        }
    }
    @Transactional
    public SimpleStatusDTO updateStatus(Integer id, SimpleStatusDTO statusDTO) {
        ValidationException validationException = new ValidationException("Validation error. Check 'errors' field for details.");
        if(statusDTO.getId() != null && id != statusDTO.getId()){
             validationException.addValidationError("id", "Id in path and body must be the same");
             throw validationException;
        }
        if(statusRepository.existsByNameAndIdNot(statusDTO.getName(), id)){
            validationException.addValidationError("name", "must be unique");
            throw validationException;
        }
        
        TaskStatus statusById = statusRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified status does not exist"));
        if (Arrays.asList(nonEditableStatuses).contains(statusById.getName())) {
            throw new BadRequestException("This status " + statusById.getName() + " cannot be updated");
        }
        try {
            statusDTO.setId(id);
            TaskStatus status = mapper.map(statusDTO, TaskStatus.class);
            TaskStatus newStatus = statusRepository.save(status);
            return mapper.map(newStatus, SimpleStatusDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to update This status");
        }
    }
    @Transactional
    public List<StatusLimitResponseDTO> updateLimitMaxiMunTask(Integer maximumTask, Boolean isLimit) {
        try {
            limitRepository.updateLimit(1, maximumTask, isLimit);
            List<StatusLimitResponseDTO> statusLimits = statusRepository.countTaskByStatus();
            statusLimits = statusLimits.stream()
                    .filter(statusLimit -> !Arrays.asList(nonEditableStatuses).contains(statusLimit.getName()))
                    .collect(Collectors.toList());
            statusLimits.forEach(statusLimit -> {
                if (statusLimit.getNoOfTasks() >= maximumTask) {
                    List<TaskList> tasks = repository.findAllByStatusId(statusLimit.getId());
                    List<TaskInLimitDTO> taskInLimitDTOS = listMapper.mapList(tasks, TaskInLimitDTO.class, mapper);
                    statusLimit.setTasks(taskInLimitDTOS);
                }
            });
            return statusLimits;
        } catch (Exception e) {
            throw new BadRequestException("Failed to update This status");
        }
    }
    @Transactional
    public SimpleStatusDTO deleteStatus(Integer id) {
        StatusCountDTO statusCount = repository.countByStatusIdAndReturnName(id);
        TaskStatus status = statusRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified status does not exist"));
        if (Arrays.asList(nonEditableStatuses).contains(status.getName())) {
            throw new BadRequestException("This status " + status.getName() + " cannot be deleted");
        }
        if(statusCount != null  && statusCount.getCount() >= 0){
            throw new BadRequestException("This status " + status.getName() + " cannot be deleted because it has tasks");
        }
        try {
            statusRepository.delete(status);
            return mapper.map(status, SimpleStatusDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to delete This status");
        }
    }
    @Transactional
    public SimpleStatusDTO deleteStatusAndTransferStatusInAllTask(Integer id, Integer newId) {
        TaskStatus status = statusRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified status for task transfer does not exist"));
        TaskStatus newStatus = statusRepository.findById(newId).orElseThrow(() -> new BadRequestException("the specified status for task transfer does not exist"));

        if(id.equals(newId)){
            throw new BadRequestException("destination status for task transfer must be different from current status" );
        }
        StatusCountDTO statusCountNewId = repository.countByStatusIdAndReturnName(newId);
        StatusCountDTO statusCount = repository.countByStatusIdAndReturnName(id);
        TaskLimit taskLimit = limitRepository.findById(1).orElseThrow(() -> new NotFoundException("Limit not found"));
        if (statusCount != null && taskLimit.getIsLimit() && statusCount.getCount() > taskLimit.getMaximumTask() && !Arrays.asList(nonEditableStatuses).contains(newStatus.getName())) {
            throw new BadRequestException("This status " + status.getName() + " has reached the maximum limit of " + taskLimit.getMaximumTask());
        }
        if (statusCountNewId !=null && (statusCountNewId.getCount() + statusCount.getCount()) > taskLimit.getMaximumTask() && taskLimit.getIsLimit()  && !Arrays.asList(nonEditableStatuses).contains(newStatus.getName())) {
            throw new BadRequestException("This task cannot be updated because the status " + statusCountNewId.getName() + " has reached the maximum limit of " + taskLimit.getMaximumTask());
        }

        try {
            repository.updateStatusId(id, newId);
            statusRepository.delete(status);
            return mapper.map(status, SimpleStatusDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to delete This status");
        }
    }


}
