// 导航栏交互逻辑
const navbar = document.querySelector('.navbar');
const navItems = document.querySelectorAll('.nav-item');
const content = document.querySelector('.content');
const pageTitles = ['缓存页面', '发现页面', '追剧页面', '设置页面', '搜索页面'];

// 事件委托处理点击事件
navbar.addEventListener('click', (e) => {
    const navItem = e.target.closest('.nav-item');
    if (navItem) {
        // 移除所有活跃状态
        navItems.forEach(item => item.classList.remove('active'));
        // 添加当前活跃状态
        navItem.classList.add('active');
        // 更新页面内容
        const index = Array.from(navItems).indexOf(navItem);
        content.querySelector('h1').textContent = pageTitles[index];
    }
});

// 键盘导航支持
navbar.addEventListener('keydown', (e) => {
    const activeElement = document.activeElement;
    const navItemIndex = Array.from(navItems).indexOf(activeElement);
    
    switch (e.key) {
        case 'ArrowLeft':
            e.preventDefault();
            const prevIndex = (navItemIndex - 1 + navItems.length) % navItems.length;
            navItems[prevIndex].focus();
            break;
        case 'ArrowRight':
            e.preventDefault();
            const nextIndex = (navItemIndex + 1) % navItems.length;
            navItems[nextIndex].focus();
            break;
        case 'Enter':
        case 'Space':
            e.preventDefault();
            activeElement.click();
            break;
    }
});

// 初始化导航栏
function initNavbar() {
    // 为每个导航项添加tabindex和role属性
    navItems.forEach((item, index) => {
        item.setAttribute('tabindex', '0');
        item.setAttribute('role', 'button');
        item.setAttribute('aria-label', pageTitles[index]);
        
        // 添加键盘焦点样式
        item.style.outline = 'none';
        item.style.transition = 'all 0.2s ease';
    });
    
    // 设置默认选中项
    navItems[1].classList.add('active');
}

// 页面加载完成后初始化
window.addEventListener('DOMContentLoaded', initNavbar);