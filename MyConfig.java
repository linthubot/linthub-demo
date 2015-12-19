package com.jayqqaa12;

import java.io.FileNotFoundException;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import com.jayqqaa12.jbase.jfinal.ext.model.Ip;
import com.jayqqaa12.jbase.jfinal.ext.xss.ACAOlHandler;
import com.jayqqaa12.jbase.jfinal.ext.xss.XssHandler;
import com.jayqqaa12.jbase.sdk.util.OSSKit;
import com.jayqqaa12.jbase.sdk.util.ZbusKit;
import com.jayqqaa12.oauth2.OauthIntercepter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.ext.handler.RenderingTimeHandler;
import com.jfinal.ext.plugin.config.ConfigKit;
import com.jfinal.ext.plugin.config.ConfigPlugin;
import com.jfinal.ext.plugin.monogodb.MongodbPlugin;
import com.jfinal.ext.plugin.quartz.QuartzPlugin;
import com.jfinal.ext.plugin.redis.JedisPlugin;
import com.jfinal.ext.plugin.sqlinxml.SqlInXmlPlugin;
import com.jfinal.ext.plugin.tablebind.AutoTableBindPlugin;
import com.jfinal.ext.route.AutoBindRoutes;
import com.jfinal.plugin.activerecord.SqlReporter;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.ehcache.EhCachePlugin;

/**
 * API引导式配置
 */
public class MyConfig extends JFinalConfig {

	private boolean isDev = isDevMode();

	public static boolean isDevMode() {
		String osName = System.getProperty("os.name");
		return osName.indexOf("Windows") != -1;
	}

	static {
		if (isDevMode()) System.setProperty("LOGDIR", "c:/");
		else System.setProperty("LOGDIR", "/log");// linux

	}

	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		new ConfigPlugin(".*.txt").reload(false).start();
		me.setDevMode(isDev);

		OSSKit.init(ConfigKit.getStr("oss.url"), ConfigKit.getStr("oss.accessId"), ConfigKit.getStr("oss.accessKey"),
				ConfigKit.getStr("oss.bucketName"), ConfigKit.getStr("oss.imgUrl"), ConfigKit.getStr("oss.domainUrl"));

		if (!isDev) ZbusKit.setDirectRpcAddr(ConfigKit.getStr("rpc.thing.device.addr"));
	}

	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		// 自动扫描 建议用注解
		AutoBindRoutes abr = new AutoBindRoutes().autoScan(false);
		me.add(abr);
	}

	/**
	 * 
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {

		// 配置Druid 数据库连接池插件
		DruidPlugin dbPlugin = new DruidPlugin(ConfigKit.getStr("jdbcUrl"), ConfigKit.getStr("user"),
				ConfigKit.getStr("password"));
		// 设置 状态监听与 sql防御
		WallFilter wall = new WallFilter();
		wall.setDbType(ConfigKit.getStr("dbType"));
		dbPlugin.addFilter(wall);
		dbPlugin.addFilter(new StatFilter());

		me.add(dbPlugin);
		// redis

		JedisPlugin jp = new JedisPlugin();
		if (isDev) jp.config("RedisConnector-test.properties");
		me.add(jp);

		me.add(new QuartzPlugin("job.properties"));
		// add EhCache
		me.add(new EhCachePlugin());
		// add sql xml plugin
		me.add(new SqlInXmlPlugin());
		// add shrio
		// me.add(new ShiroPlugin(this.routes));

		// 配置AutoTableBindPlugin插件
		AutoTableBindPlugin atbp = new AutoTableBindPlugin(dbPlugin);
		atbp.addIncludeClasses(Ip.class);
		if (isDev) atbp.setShowSql(true);
		atbp.autoScan(false);
		me.add(atbp);
		// sql记录
		SqlReporter.setLogger(true);

		me.add(new MongodbPlugin(ConfigKit.getStr("mongo.url"), 27017, ConfigKit.getStr("mongo.db")));

	}

	@Override
	public void afterJFinalStart() {

//		try {
//			String url;
//			url = OSSKit.uploadFile("test2.mp4", "c:/1.mp4",false,"video/mp4");
//			
//			System.out.println(url);
//
//			url = OSSKit.uploadFile("test3.mp4", "c:/2.mp4",false,"video/mp4");
//
//			System.out.println(url);
//		} catch (OSSException | ClientException | FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		

	}

	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {

		// me.add(new IocInterceptor());
		// shiro权限拦截器配置
		// me.add(new ShiroInterceptor());
		// 让 模版 可以使用session
		// me.add(new SessionInViewInterceptor());
		me.add(new JsonExceptionInterceptor());
		// 对 api 接口进行 oauth2 认证。
		if (!isDev) me.add(new OauthIntercepter("/api/"));
	}

	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		// 计算每个page 运行时间
		me.add(new RenderingTimeHandler());
		// xss 过滤
		me.add(new XssHandler("s"));
		// 伪静态处理
		// me.add(new FakeStaticHandler());
		// 去掉 jsessionid 防止找不到action
		// me.add(new com.jayqqaa12.shiro.SessionHandler());
		me.add(new DruidStatViewHandler("/druid"));

		// 允许跨域
		me.add(new ACAOlHandler("*"));

	}

	/**
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {

		JFinal.start("src/main/webapp", 2222, "/", 5);
	}

}
