package com.dongnao.mvc.servlet;

import com.dongnao.mvc.annotation.MyController;
import com.dongnao.mvc.annotation.MyQualifier;
import com.dongnao.mvc.annotation.MyRequestMapping;
import com.dongnao.mvc.annotation.MyService;
import com.dongnao.mvc.util.ClassBeanUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class MyServletDispather extends HttpServlet {

    // 读取配置
    private Properties properties = new Properties();

    // 类的全路径集合
    private List<String> classNames = new ArrayList<String>();
    // IOC容器
    private Map<String, Object> iocConent = new HashMap<String, Object>();
    // 请求映射
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();
    // REQUEST_IOC
//    private Map<String, Object> controllerIocContent = new HashMap<String, Object>();

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        // 加载配置文件
        doLoadConfig(servletConfig.getInitParameter("contextConfigLocation"));

        // 初始化相关类，扫描包
        doScanner(properties.getProperty("scan.package"));

        // 反射，实例化类，放入IOC
        doInstance();

        // 初始化HandlerMapping
        initHandlerMapping();

        // 实现注入
        doIOC();

    }

    private void doLoadConfig(String contextConfigLocation) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫描包下面所有的类文件
     * @param packagePath
     */
    private void doScanner(String packagePath) {
        URL url = this.getClass().getClassLoader().getResource("/" + packagePath.replaceAll("\\.", "/"));
        File file = new File(url.getFile());
        String[] fileList = file.list();
        for (String path : fileList) {
            File cacheFile = new File(url.getFile() + "\\" + path);
            if (cacheFile.isDirectory()) {
                doScanner(packagePath + "." + cacheFile.getName());
            } else {
                classNames.add(packagePath + "." + cacheFile.getName().replaceAll(".class","").trim());
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Object objBean = null;
                boolean createFlag = false;
                String objKey = "";
                if (clazz.isAnnotationPresent(MyController.class)) {
                    createFlag = true;
                    MyController myController = clazz.getAnnotation(MyController.class);
                    objKey = myController.value();
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    createFlag = true;
                    MyService myService = clazz.getAnnotation(MyService.class);
                    objKey = myService.value();
                } else {
                    continue;
                }

                if (createFlag) {
                    objBean = clazz.newInstance();
                    if (objKey==null || "".endsWith(objKey.trim())) {
                        objKey = getObjectKey(clazz);
                    }
                    iocConent.put(objKey, objBean);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private String getObjectKey(Class<?> clazz) {
        return ClassBeanUtils.toLowerFirstWord(clazz.getSimpleName());
    }

    private void initHandlerMapping() {
        if (iocConent.isEmpty()) {
            return;
        }
        Map<String, Object> controllerIocContent = new HashMap<String, Object>();
        for (String key : iocConent.keySet()) {
            Object objBean = iocConent.get(key);
            // 判断是否是http请求处理类
            Class clazz = objBean.getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }

            // 组织请求地址：1、类拦截地址；2、方法拦截地址
            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMappingAnnotation = (MyRequestMapping) clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMappingAnnotation.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }

                String url = "";
                MyRequestMapping methodRequestMappingAnnotation = method.getAnnotation(MyRequestMapping.class);
                String requestMappingurl = methodRequestMappingAnnotation.value();
                url = baseUrl + requestMappingurl;
                handlerMapping.put(url, method);

                controllerIocContent.put(url, objBean);
            }
        }
        iocConent.putAll(controllerIocContent);
    }

    private void doIOC() {
        if (iocConent.isEmpty()) {
            return ;
        }

        for (Map.Entry<String, Object> entry: iocConent.entrySet()) {
            // 拿到所有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(MyQualifier.class)) {
                    MyQualifier myQualifier = field.getAnnotation(MyQualifier.class);
                    String value = myQualifier.value();
                    String key = "";
                    if (null!=value && !"".equals(value)) {
                        key = value;
                    } else {
//                        key = field.getName();
                        Class fileClass = field.getClass();
                        key = getObjectKey(fileClass);
                    }
                    try {
                        field.set(entry.getValue(), iocConent.get(key));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispather(req, resp);
    }

    public void doDispather(HttpServletRequest req, HttpServletResponse resp) {
        if (handlerMapping.isEmpty()) {
            return;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();

        // 拼接url并把多个“/”替换成一个
        String iocKey = url.replaceAll(contextPath,"").replaceAll("/+", "/");
        if (!handlerMapping.containsKey(url)) {
            try {
                resp.getWriter().write("404 request not found");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        Method method = handlerMapping.get(url);
        // 获取方法的参数列表:(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String name)
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 保存参数值
        Object[] paramValues = new Object[parameterTypes.length];
        for (int i=0; i<parameterTypes.length; i++) {
            String requestParam = parameterTypes[i].getSimpleName();
            if (requestParam.equals("HttpServletRequest")) {
                paramValues[i] = req;
                continue;
            }
            if (requestParam.equals("HttpServletResponse")) {
                paramValues[i] = resp;
                continue;
            }
            if (requestParam.equals("String")) {
                for (Map.Entry<String, String[]> param :
                        parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i] = value;
                }
            }
        }

        // 利用反射机制来调用
        try {
            method.invoke(this.iocConent.get(url), paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
