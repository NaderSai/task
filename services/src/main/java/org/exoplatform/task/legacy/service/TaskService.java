package org.exoplatform.task.legacy.service;

import java.util.List;
import java.util.Set;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.ChangeLog;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;

/**
 * Created by TClement on 6/3/15.
 */
public interface TaskService {

  String TASK_CREATION = "exo.task.taskCreation";

  String TASK_UPDATE = "exo.task.taskUpdate";

  String TASK_COMMENT_CREATION = "exo.task.taskCommentCreation";

  /**
   * Create a new task.
   *
   * @param task
   * @return the created task.
   */
  Task createTask(Task task);

  /**
   * Update the task.
   *
   * @param task
   * @return the updated task.
   */
  Task updateTask(Task task);

  void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders);

  /**
   * Remove the task with given <code>taskId</code>
   *
   * @param taskId
   * @throws EntityNotFoundException
   */
  void removeTask(long taskId) throws EntityNotFoundException;

  /**
   * Clone the task from a task with given <code>taskId</code>.
   */
  Task cloneTask(long taskId) throws EntityNotFoundException;

  /**
   * Return the task with given <code>taskId</code>.
   *
   * @param taskId
   * @return
   * @throws EntityNotFoundException
   */
  Task getTask(long taskId) throws EntityNotFoundException;

  Comment getComment(long commentId);

  ListAccess<Comment> getComments(long taskId);

  Comment addComment(long taskId, String username, String commentText) throws EntityNotFoundException;

  Comment addComment(long id, long parentCommentId, String username, String comment) throws EntityNotFoundException;

  void removeComment(long commentId) throws EntityNotFoundException;

  ListAccess<Task> findTasks(TaskQuery query);

  public <T> List<T> selectTaskField(TaskQuery query, String fieldName);

  ListAccess<Task> findTasksByLabel(long labelId, List<Long> projectIds, String username, OrderBy orderBy) throws EntityNotFoundException;

  /**
   * Create a log associated with a task with given <code>taskId</code>.
   *
   * @param taskId
   * @param username
   * @param actionName
   * @param target
   * @return
   * @throws EntityNotFoundException
   */
  ChangeLog addTaskLog(long taskId, String username, String actionName, String target) throws EntityNotFoundException;

  void addTaskToLabel(Long taskId, Long labelId) throws EntityNotFoundException;

  void removeTaskFromLabel(Long taskId, Long labelId) throws EntityNotFoundException;

  ListAccess<Label> findLabelsByUser(String username);

  ListAccess<Label> findLabelsByTask(long taskId, String username) throws EntityNotFoundException;

  Label getLabel(long labelId);

  Label createLabel(Label label);

  Label updateLabel(Label label, List<Label.FIELDS> fields) throws EntityNotFoundException;

  void removeLabel(long labelId);

  ListAccess<ChangeLog> getTaskLogs(long taskId);

  //TODO: should use via #findTasks(TaskQuery)?
  Task findTaskByActivityId(String activityId);

  Set<String> getCoworker(long taskId);

  /**
   * Fetch sub comments of designed comments
   * 
   * @param listComments
   */
  void loadSubComments(List<Comment> listComments);

  Set<String> getMentionedUsers(long taskId);

  List<Task> getUncompletedTasks(String user, int limit);

  Long countUncompletedTasks(String user);
  
  ListAccess<Task> getIncomingTasks(String user);
  
  List<Task> getOverdueTasks(String user, int limit);
  
  Long countOverdueTasks(String user);

  Set<String> getWatchersOfTask(Task task) ;

  void addWatcherToTask(String username, Task task) throws Exception;

  void deleteWatcherOfTask(String username, Task task) throws Exception;

  /**
   * Find tasks assigned to a user using a term to find in title or description
   * of the task
   * 
   * @param user username
   * @param query term to search in title or description
   * @param limit
   * @return {@link List} of {@link Task}
   */
  List<Task> findTasks(String user, String query, int limit);

  /**
   * Count tasks assigned to a user using a search term to find in title or
   * description of the task
   * 
   * @param user username
   * @param query term to search in title or description
   * @return tasks count
   */
  long countTasks(String user, String query);
}
