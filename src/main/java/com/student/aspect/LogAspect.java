package com.student.aspect;

import com.student.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogService operationLogService;

    @Around("execution(* com.student.controller.GradeController.batchSave(..))")
    public Object logGradeBatchSave(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        recordLog("成绩批量录入", "grade", null, "批量录入成绩");
        return result;
    }

    @Around("execution(* com.student.controller.GradeController.updateScore(..))")
    public Object logGradeUpdate(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Long id = (Long) args[0];
        Object result = pjp.proceed();
        recordLog("成绩修改", "grade", id, "修改成绩id=" + id);
        return result;
    }

    @Around("execution(* com.student.controller.SelectionController.drop(..))")
    public Object logSelectionDrop(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Long id = (Long) args[0];
        Object result = pjp.proceed();
        recordLog("退课", "selection", id, "退课selectionId=" + id);
        return result;
    }

    @Around("execution(* com.student.controller.SelectionController.select(..))")
    public Object logSelectionAdd(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        recordLog("选课", "selection", null, "学生选课");
        return result;
    }

    @Around("execution(* com.student.controller.StudentController.delete*(..)) || " +
            "execution(* com.student.controller.TeacherController.delete*(..)) || " +
            "execution(* com.student.controller.CourseController.delete*(..)) || " +
            "execution(* com.student.controller.OpeningController.delete*(..))")
    public Object logDelete(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().getSimpleName();
        String targetType = className.replace("Controller", "").toLowerCase();
        Object[] args = pjp.getArgs();
        Long id = args.length > 0 && args[0] instanceof Long ? (Long) args[0] : null;
        Object result = pjp.proceed();
        recordLog("删除" + targetType, targetType, id, "删除" + targetType + " id=" + id);
        return result;
    }

    private void recordLog(String operation, String targetType, Long targetId, String detail) {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String operator = "unknown";
            String role = "unknown";
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                Object uid = req.getAttribute("userId");
                Object r = req.getAttribute("role");
                operator = uid != null ? uid.toString() : "anonymous";
                role = r != null ? r.toString() : "anonymous";
            }
            operationLogService.log(operation, operator, role, targetType, targetId, detail);
        } catch (Exception e) {
            log.warn("Failed to record operation log: {}", e.getMessage());
        }
    }
}
