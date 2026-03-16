# EmbyPlayer UI导航和内容显示 - 实现计划

## [x] 任务 1: 实现固定浮动导航栏
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - 在应用底部实现三个固定的浮动导航按钮
  - 确保按钮始终可见，不受页面滚动或其他交互影响
  - 实现按钮的点击事件处理
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement` TR-1.1: 导航按钮是否始终固定在底部，位置不随页面滚动变化
  - `human-judgement` TR-1.2: 按钮样式是否统一，视觉效果是否良好
- **Notes**: 考虑使用FrameLayout或CoordinatorLayout实现固定效果

## [x] 任务 2: 实现页面切换功能
- **Priority**: P0
- **Depends On**: 任务 1
- **Description**:
  - 实现页面切换逻辑，点击导航按钮时替换当前视图
  - 实现平滑的页面过渡效果
  - 确保页面切换时导航按钮位置不变
- **Acceptance Criteria Addressed**: AC-2, AC-6
- **Test Requirements**:
  - `human-judgement` TR-2.1: 点击导航按钮是否能正确切换页面
  - `human-judgement` TR-2.2: 页面切换过渡是否平滑，无卡顿或闪烁
- **Notes**: 考虑使用Fragment或ViewPager2实现页面切换

## [x] 任务 3: 实现左右两侧浮动元素
- **Priority**: P1
- **Depends On**: 任务 1
- **Description**:
  - 在界面左右两侧实现浮动元素
  - 实现根据当前页面动态更新浮动元素的逻辑
  - 确保浮动元素与导航按钮样式一致
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `human-judgement` TR-3.1: 浮动元素是否正确显示在左右两侧
  - `human-judgement` TR-3.2: 页面切换时浮动元素是否正确更新
  - `human-judgement` TR-3.3: 浮动元素样式是否与导航按钮一致
- **Notes**: 考虑使用绝对定位或约束布局实现浮动效果

## [x] 任务 4: 优化主页内容显示
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - 实现按媒体服务器库顺序展示内容的逻辑
  - 移除现有的内容分类
  - 替换为媒体服务器的分类结构
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - `human-judgement` TR-4.1: 主页内容是否按媒体服务器库顺序显示
  - `human-judgement` TR-4.2: 是否已移除现有内容分类
  - `human-judgement` TR-4.3: 是否已替换为媒体服务器的分类结构
- **Notes**: 需要调用媒体服务器API获取库结构信息

## [x] 任务 5: 实现视频媒体轮播功能
- **Priority**: P1
- **Depends On**: 任务 4
- **Description**:
  - 在主页顶部实现视频媒体轮播功能
  - 确保轮播效果平滑，自动播放
  - 轮播内容应来自媒体服务器
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `human-judgement` TR-5.1: 视频轮播是否正确显示在主页顶部
  - `human-judgement` TR-5.2: 轮播效果是否平滑，自动播放
  - `human-judgement` TR-5.3: 轮播内容是否来自媒体服务器
- **Notes**: 考虑使用ViewPager2或第三方轮播库实现

## [x] 任务 6: 整体测试与优化
- **Priority**: P1
- **Depends On**: 任务 1, 任务 2, 任务 3, 任务 4, 任务 5
- **Description**:
  - 测试所有功能是否正常工作
  - 优化页面切换性能
  - 确保所有元素样式一致
- **Acceptance Criteria Addressed**: 所有
- **Test Requirements**:
  - `human-judgement` TR-6.1: 所有功能是否正常工作
  - `human-judgement` TR-6.2: 页面切换是否流畅
  - `human-judgement` TR-6.3: 整体视觉效果是否一致、美观
- **Notes**: 测试不同设备和屏幕尺寸的适配情况