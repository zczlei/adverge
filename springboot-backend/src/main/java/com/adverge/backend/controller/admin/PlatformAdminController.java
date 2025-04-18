package com.adverge.backend.controller.admin;

import com.adverge.backend.model.Config;
import com.adverge.backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 广告平台管理界面控制器
 * 提供Web界面用于管理广告平台配置
 */
@Slf4j
@Controller
@RequestMapping("/admin/platforms")
@RequiredArgsConstructor
public class PlatformAdminController {

    private final ConfigService configService;

    /**
     * 平台列表页面
     */
    @GetMapping
    public String platformList(Model model) {
        List<Config.Platform> platforms = configService.getPlatforms();
        model.addAttribute("platforms", platforms);
        return "admin/platforms/list";
    }

    /**
     * 创建平台页面
     */
    @GetMapping("/create")
    public String createPlatformForm(Model model) {
        model.addAttribute("platform", new Config.Platform());
        return "admin/platforms/form";
    }

    /**
     * 创建平台处理
     */
    @PostMapping("/create")
    public String createPlatform(
            @ModelAttribute Config.Platform platform,
            RedirectAttributes redirectAttributes) {
        
        try {
            configService.savePlatform(platform);
            redirectAttributes.addFlashAttribute("success", "平台创建成功");
            return "redirect:/admin/platforms";
        } catch (Exception e) {
            log.error("创建平台失败", e);
            redirectAttributes.addFlashAttribute("error", "平台创建失败: " + e.getMessage());
            return "redirect:/admin/platforms/create";
        }
    }

    /**
     * 编辑平台页面
     */
    @GetMapping("/edit/{name}")
    public String editPlatformForm(
            @PathVariable String name,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        Config.Platform platform = configService.getPlatform(name);
        if (platform == null) {
            redirectAttributes.addFlashAttribute("error", "平台不存在");
            return "redirect:/admin/platforms";
        }
        
        model.addAttribute("platform", platform);
        return "admin/platforms/form";
    }

    /**
     * 编辑平台处理
     */
    @PostMapping("/edit/{name}")
    public String updatePlatform(
            @PathVariable String name,
            @ModelAttribute Config.Platform platform,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 确保平台名称一致
            platform.setName(name);
            
            configService.savePlatform(platform);
            redirectAttributes.addFlashAttribute("success", "平台更新成功");
            return "redirect:/admin/platforms";
        } catch (Exception e) {
            log.error("更新平台失败", e);
            redirectAttributes.addFlashAttribute("error", "平台更新失败: " + e.getMessage());
            return "redirect:/admin/platforms/edit/" + name;
        }
    }

    /**
     * 启用平台
     */
    @PostMapping("/enable/{name}")
    public String enablePlatform(
            @PathVariable String name,
            RedirectAttributes redirectAttributes) {
        
        try {
            configService.setPlatformEnabled(name, true);
            redirectAttributes.addFlashAttribute("success", "平台已启用");
        } catch (Exception e) {
            log.error("启用平台失败", e);
            redirectAttributes.addFlashAttribute("error", "启用平台失败: " + e.getMessage());
        }
        
        return "redirect:/admin/platforms";
    }

    /**
     * 禁用平台
     */
    @PostMapping("/disable/{name}")
    public String disablePlatform(
            @PathVariable String name,
            RedirectAttributes redirectAttributes) {
        
        try {
            configService.setPlatformEnabled(name, false);
            redirectAttributes.addFlashAttribute("success", "平台已禁用");
        } catch (Exception e) {
            log.error("禁用平台失败", e);
            redirectAttributes.addFlashAttribute("error", "禁用平台失败: " + e.getMessage());
        }
        
        return "redirect:/admin/platforms";
    }

    /**
     * 删除平台
     */
    @PostMapping("/delete/{name}")
    public String deletePlatform(
            @PathVariable String name,
            RedirectAttributes redirectAttributes) {
        
        try {
            boolean deleted = configService.deletePlatform(name);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "平台已删除");
            } else {
                redirectAttributes.addFlashAttribute("error", "平台不存在");
            }
        } catch (Exception e) {
            log.error("删除平台失败", e);
            redirectAttributes.addFlashAttribute("error", "删除平台失败: " + e.getMessage());
        }
        
        return "redirect:/admin/platforms";
    }
} 