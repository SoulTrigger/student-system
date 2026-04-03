package com.student.system.context;

public class RequestContextHolder {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<String> role = new ThreadLocal<>();
    private static final ThreadLocal<String> name = new ThreadLocal<>();

    public static void set(Long uid, String r, String n) {
        userId.set(uid);
        role.set(r);
        name.set(n);
    }

    public static Long getUserId() { return userId.get(); }
    public static String getRole() { return role.get(); }
    public static String getName() { return name.get(); }

    public static void clear() {
        userId.remove();
        role.remove();
        name.remove();
    }
}
