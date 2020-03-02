### Spring Security整合

##### 示例过程
   - 定义相对应的视图访问，可以使用@Controller定义，也可继承WebMvcConfigurerAdapter，重写addViewControllers方法，增加视图
   - 继承WebSecurityConfigurerAdapter来重写页面访问的权限，以及认证管理
##### 用途
   - 结合Shiro，可以进行权限控制
   - 结合Oauth2，可以进行第三方服务授权等功能