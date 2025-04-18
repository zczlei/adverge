package com.adverge.backend.controller.admin;

import com.adverge.backend.model.AdUnit;
import com.adverge.backend.model.App;
import com.adverge.backend.service.AdUnitService;
import com.adverge.backend.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * 应用管理界面控制器
 * 提供Web界面用于管理应用配置
 */
@Slf4j
@Controller
@RequestMapping("/admin/apps")
@RequiredArgsConstructor
public class AppAdminController {

    private final AppService appService;
    private final AdUnitService adUnitService;

    /**
     * 应用列表页面
     */
    @GetMapping
    public String appList(Model model) {
        List<App> apps = appService.getAllApps();
        model.addAttribute("apps", apps);
        return "admin/apps/list";
    }

    /**
     * 创建应用页面
     */
    @GetMapping("/create")
    public String createAppForm(Model model) {
        model.addAttribute("app", new App());
        model.addAttribute("isNew", true);
        return "admin/apps/form";
    }

    /**
     * 创建应用处理
     */
    @PostMapping("/create")
    public String createApp(
            @Valid @ModelAttribute App app,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isNew", true);
            return "admin/apps/form";
        }
        
        try {
            // 确保新应用有初始化的参数
            if (app.getAdUnitIds() == null) {
                app.setAdUnitIds(Collections.emptyList());
            }
            if (app.getApiKey() == null || app.getApiKey().isEmpty()) {
                app.generateApiKey();
            }
            
            // 保存应用
            App savedApp = appService.saveApp(app);
            
            redirectAttributes.addFlashAttribute("success", "应用 '" + savedApp.getName() + "' 创建成功");
            return "redirect:/admin/apps";
        } catch (Exception e) {
            log.error("创建应用失败", e);
            redirectAttributes.addFlashAttribute("error", "应用创建失败: " + e.getMessage());
            return "redirect:/admin/apps/create";
        }
    }

    /**
     * 编辑应用页面
     */
    @GetMapping("/edit/{id}")
    public String editAppForm(
            @PathVariable String id,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            App app = appService.getAppById(id)
                    .orElseThrow(() -> new IllegalArgumentException("未找到ID为 " + id + " 的应用"));
            
            model.addAttribute("app", app);
            model.addAttribute("isNew", false);
            
            return "admin/apps/form";
        } catch (Exception e) {
            log.error("获取应用失败", e);
            redirectAttributes.addFlashAttribute("error", "获取应用失败: " + e.getMessage());
            return "redirect:/admin/apps";
        }
    }

    /**
     * 编辑应用处理
     */
    @PostMapping("/edit/{id}")
    public String updateApp(
            @PathVariable String id,
            @Valid @ModelAttribute App app,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isNew", false);
            return "admin/apps/form";
        }
        
        try {
            // 确保应用ID一致
            app.setId(id);
            
            // 获取原始应用以保留不在表单中的字段
            App originalApp = appService.getAppById(id)
                    .orElseThrow(() -> new IllegalArgumentException("未找到ID为 " + id + " 的应用"));
            
            // 保留原始的广告位ID列表和API密钥
            app.setAdUnitIds(originalApp.getAdUnitIds());
            app.setApiKey(originalApp.getApiKey());
            
            // 保存更新
            App updatedApp = appService.saveApp(app);
            
            redirectAttributes.addFlashAttribute("success", "应用 '" + updatedApp.getName() + "' 更新成功");
            return "redirect:/admin/apps";
        } catch (Exception e) {
            log.error("更新应用失败", e);
            redirectAttributes.addFlashAttribute("error", "应用更新失败: " + e.getMessage());
            return "redirect:/admin/apps/edit/" + id;
        }
    }

    /**
     * 删除应用
     */
    @PostMapping("/delete/{id}")
    public String deleteApp(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {
        
        try {
            App app = appService.getAppById(id)
                    .orElseThrow(() -> new IllegalArgumentException("未找到ID为 " + id + " 的应用"));
            
            // 删除应用关联的所有广告位
            if (app.getAdUnitIds() != null && !app.getAdUnitIds().isEmpty()) {
                for (String adUnitId : app.getAdUnitIds()) {
                    try {
                        adUnitService.deleteAdUnit(adUnitId);
                    } catch (Exception e) {
                        log.warn("删除应用 {} 关联的广告位 {} 失败: {}", id, adUnitId, e.getMessage());
                    }
                }
            }
            
            // 删除应用
            appService.deleteApp(id);
            
            redirectAttributes.addFlashAttribute("success", "应用 '" + app.getName() + "' 及其关联的广告位已删除");
            return "redirect:/admin/apps";
        } catch (Exception e) {
            log.error("删除应用失败", e);
            redirectAttributes.addFlashAttribute("error", "删除应用失败: " + e.getMessage());
            return "redirect:/admin/apps";
        }
    }

    /**
     * 查看应用详情
     */
    @GetMapping("/view/{id}")
    public String viewApp(
            @PathVariable String id,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            App app = appService.getAppById(id)
                    .orElseThrow(() -> new IllegalArgumentException("未找到ID为 " + id + " 的应用"));
            
            model.addAttribute("app", app);
            
            // 获取应用关联的广告位
            List<AdUnit> adUnits = Collections.emptyList();
            if (app.getAdUnitIds() != null && !app.getAdUnitIds().isEmpty()) {
                adUnits = adUnitService.getAdUnitsByIds(app.getAdUnitIds());
            }
            model.addAttribute("adUnits", adUnits);
            
            return "admin/apps/view";
        } catch (Exception e) {
            log.error("获取应用详情失败", e);
            redirectAttributes.addFlashAttribute("error", "获取应用详情失败: " + e.getMessage());
            return "redirect:/admin/apps";
        }
    }

    /**
     * 重新生成API密钥
     */
    @PostMapping("/{id}/regenerate-api-key")
    public String regenerateApiKey(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {
        
        try {
            return appService.regenerateApiKey(id)
                    .map(app -> {
                        redirectAttributes.addFlashAttribute("success", "应用 '" + app.getName() + "' 的API密钥已重新生成");
                        return "redirect:/admin/apps/view/" + id;
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("error", "未找到ID为 " + id + " 的应用");
                        return "redirect:/admin/apps";
                    });
        } catch (Exception e) {
            log.error("重新生成API密钥失败", e);
            redirectAttributes.addFlashAttribute("error", "重新生成API密钥失败: " + e.getMessage());
            return "redirect:/admin/apps/view/" + id;
        }
    }

    /**
     * 切换应用状态（启用/禁用）
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleAppStatus(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {
        
        try {
            App app = appService.getAppById(id)
                    .orElseThrow(() -> new IllegalArgumentException("未找到ID为 " + id + " 的应用"));
            
            // 切换状态
            boolean newStatus = !app.isEnabled();
            appService.setAppEnabled(id, newStatus);
            
            String statusMessage = newStatus ? "启用" : "禁用";
            redirectAttributes.addFlashAttribute("success", "应用 '" + app.getName() + "' 已" + statusMessage);
            return "redirect:/admin/apps";
        } catch (Exception e) {
            log.error("切换应用状态失败", e);
            redirectAttributes.addFlashAttribute("error", "切换应用状态失败: " + e.getMessage());
            return "redirect:/admin/apps";
        }
    }
} 