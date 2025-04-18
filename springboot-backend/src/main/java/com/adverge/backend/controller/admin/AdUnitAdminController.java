package com.adverge.backend.controller.admin;

import com.adverge.backend.dto.AdUnitRequest;
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
import java.util.List;
import java.util.Optional;

/**
 * 广告位管理界面控制器
 * 提供Web界面用于管理广告位配置
 */
@Slf4j
@Controller
@RequestMapping("/admin/ad-units")
@RequiredArgsConstructor
public class AdUnitAdminController {

    private final AdUnitService adUnitService;
    private final AppService appService;

    /**
     * 广告位列表页面
     */
    @GetMapping
    public String adUnitList(Model model) {
        List<AdUnit> adUnits = adUnitService.getAllAdUnits();
        model.addAttribute("adUnits", adUnits);
        
        // 获取所有应用，用于显示应用名称
        List<App> apps = appService.getAllApps();
        model.addAttribute("apps", apps);
        
        return "admin/ad-units/list";
    }

    /**
     * 创建广告位页面
     */
    @GetMapping("/create")
    public String createAdUnitForm(
            @RequestParam(required = false) String appId,
            Model model) {
        
        AdUnitRequest adUnitRequest = new AdUnitRequest();
        
        // 如果指定了应用ID，则预设应用ID
        if (appId != null && !appId.isEmpty()) {
            adUnitRequest.setAppId(appId);
        }
        
        model.addAttribute("adUnitRequest", adUnitRequest);
        model.addAttribute("isNew", true);
        
        // 获取所有应用，用于下拉选择框
        List<App> apps = appService.getAllApps();
        model.addAttribute("apps", apps);
        
        return "admin/ad-units/form";
    }

    /**
     * 创建广告位处理
     */
    @PostMapping("/create")
    public String createAdUnit(
            @Valid @ModelAttribute AdUnitRequest adUnitRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isNew", true);
            List<App> apps = appService.getAllApps();
            model.addAttribute("apps", apps);
            return "admin/ad-units/form";
        }
        
        try {
            // 创建广告位
            AdUnit adUnit = adUnitService.createAdUnit(adUnitRequest);
            
            // 更新应用的广告位关联
            appService.addAdUnitId(adUnitRequest.getAppId(), adUnit.getId());
            
            redirectAttributes.addFlashAttribute("success", "广告位 '" + adUnit.getName() + "' 创建成功");
            
            // 如果是来自应用详情页的创建，则重定向回应用详情页
            if (adUnitRequest.getAppId() != null && !adUnitRequest.getAppId().isEmpty()) {
                return "redirect:/admin/apps/view/" + adUnitRequest.getAppId();
            }
            
            return "redirect:/admin/ad-units";
        } catch (Exception e) {
            log.error("创建广告位失败", e);
            redirectAttributes.addFlashAttribute("error", "广告位创建失败: " + e.getMessage());
            
            // 如果是来自应用详情页的创建，则重定向回应用详情页
            if (adUnitRequest.getAppId() != null && !adUnitRequest.getAppId().isEmpty()) {
                return "redirect:/admin/apps/view/" + adUnitRequest.getAppId();
            }
            
            return "redirect:/admin/ad-units/create";
        }
    }

    /**
     * 编辑广告位页面
     */
    @GetMapping("/edit/{id}")
    public String editAdUnitForm(
            @PathVariable String id,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            AdUnit adUnit = adUnitService.getAdUnitById(id);
            
            // 将AdUnit转换为AdUnitRequest
            AdUnitRequest adUnitRequest = new AdUnitRequest();
            adUnitRequest.setName(adUnit.getName());
            adUnitRequest.setAppId(adUnit.getAppId());
            adUnitRequest.setType(adUnit.getType());
            adUnitRequest.setDescription(adUnit.getDescription());
            adUnitRequest.setActive(adUnit.isActive());
            adUnitRequest.setFloorPrice(adUnit.getFloorPrice());
            adUnitRequest.setRefreshInterval(adUnit.getRefreshInterval());
            adUnitRequest.setPosition(adUnit.getPosition());
            adUnitRequest.setSize(adUnit.getSize());
            
            model.addAttribute("adUnitRequest", adUnitRequest);
            model.addAttribute("adUnitId", id);
            model.addAttribute("isNew", false);
            
            // 获取所有应用，用于下拉选择框
            List<App> apps = appService.getAllApps();
            model.addAttribute("apps", apps);
            
            // 保存原应用ID，用于处理应用关联的更新
            model.addAttribute("originalAppId", adUnit.getAppId());
            
            return "admin/ad-units/form";
        } catch (Exception e) {
            log.error("获取广告位失败", e);
            redirectAttributes.addFlashAttribute("error", "获取广告位失败: " + e.getMessage());
            return "redirect:/admin/ad-units";
        }
    }

    /**
     * 编辑广告位处理
     */
    @PostMapping("/edit/{id}")
    public String updateAdUnit(
            @PathVariable String id,
            @Valid @ModelAttribute AdUnitRequest adUnitRequest,
            BindingResult bindingResult,
            @RequestParam(required = false) String originalAppId,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("adUnitId", id);
            model.addAttribute("isNew", false);
            List<App> apps = appService.getAllApps();
            model.addAttribute("apps", apps);
            model.addAttribute("originalAppId", originalAppId);
            return "admin/ad-units/form";
        }
        
        try {
            // 更新广告位
            AdUnit updatedAdUnit = adUnitService.updateAdUnit(id, adUnitRequest);
            
            // 处理应用关联的更新
            if (originalAppId != null && !originalAppId.equals(adUnitRequest.getAppId())) {
                // 从原应用移除广告位ID
                appService.removeAdUnitId(originalAppId, id);
                
                // 添加到新应用
                appService.addAdUnitId(adUnitRequest.getAppId(), id);
            }
            
            redirectAttributes.addFlashAttribute("success", "广告位 '" + updatedAdUnit.getName() + "' 更新成功");
            
            // 如果有应用ID参数，则重定向回应用详情页
            if (adUnitRequest.getAppId() != null && !adUnitRequest.getAppId().isEmpty()) {
                return "redirect:/admin/apps/view/" + adUnitRequest.getAppId();
            }
            
            return "redirect:/admin/ad-units";
        } catch (Exception e) {
            log.error("更新广告位失败", e);
            redirectAttributes.addFlashAttribute("error", "广告位更新失败: " + e.getMessage());
            return "redirect:/admin/ad-units/edit/" + id;
        }
    }

    /**
     * 删除广告位
     */
    @PostMapping("/delete/{id}")
    public String deleteAdUnit(
            @PathVariable String id,
            @RequestParam(required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {
        
        try {
            AdUnit adUnit = adUnitService.getAdUnitById(id);
            String adUnitName = adUnit.getName();
            String appId = adUnit.getAppId();
            
            // 删除广告位
            adUnitService.deleteAdUnit(id);
            
            // 从应用中移除广告位关联
            appService.removeAdUnitId(appId, id);
            
            redirectAttributes.addFlashAttribute("success", "广告位 '" + adUnitName + "' 已删除");
            
            // 如果指定了返回URL，则重定向到该URL
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl;
            }
            
            // 如果有appId，返回到应用详情页
            if (appId != null && !appId.isEmpty()) {
                return "redirect:/admin/apps/view/" + appId;
            }
            
            return "redirect:/admin/ad-units";
        } catch (Exception e) {
            log.error("删除广告位失败", e);
            redirectAttributes.addFlashAttribute("error", "删除广告位失败: " + e.getMessage());
            
            // 如果指定了返回URL，则重定向到该URL
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl;
            }
            
            return "redirect:/admin/ad-units";
        }
    }

    /**
     * 切换广告位状态（启用/禁用）
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleAdUnitStatus(
            @PathVariable String id,
            @RequestParam(required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {
        
        try {
            AdUnit adUnit = adUnitService.getAdUnitById(id);
            // 切换状态
            boolean newStatus = !adUnit.isActive();
            
            AdUnitRequest request = new AdUnitRequest();
            request.setName(adUnit.getName());
            request.setAppId(adUnit.getAppId());
            request.setType(adUnit.getType());
            request.setDescription(adUnit.getDescription());
            request.setActive(newStatus);
            request.setFloorPrice(adUnit.getFloorPrice());
            request.setRefreshInterval(adUnit.getRefreshInterval());
            request.setPosition(adUnit.getPosition());
            request.setSize(adUnit.getSize());
            
            adUnitService.updateAdUnit(id, request);
            
            String statusMessage = newStatus ? "启用" : "禁用";
            redirectAttributes.addFlashAttribute("success", "广告位 '" + adUnit.getName() + "' 已" + statusMessage);
            
            // 如果指定了返回URL，则重定向到该URL
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl;
            }
            
            // 如果有appId，返回到应用详情页
            if (adUnit.getAppId() != null && !adUnit.getAppId().isEmpty()) {
                return "redirect:/admin/apps/view/" + adUnit.getAppId();
            }
            
            return "redirect:/admin/ad-units";
        } catch (Exception e) {
            log.error("切换广告位状态失败", e);
            redirectAttributes.addFlashAttribute("error", "切换广告位状态失败: " + e.getMessage());
            
            // 如果指定了返回URL，则重定向到该URL
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl;
            }
            
            return "redirect:/admin/ad-units";
        }
    }

    /**
     * 查看广告位详情
     */
    @GetMapping("/view/{id}")
    public String viewAdUnit(
            @PathVariable String id,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            AdUnit adUnit = adUnitService.getAdUnitById(id);
            model.addAttribute("adUnit", adUnit);
            
            // 获取关联的应用信息
            Optional<App> appOpt = appService.getAppById(adUnit.getAppId());
            appOpt.ifPresent(app -> model.addAttribute("app", app));
            
            return "admin/ad-units/view";
        } catch (Exception e) {
            log.error("获取广告位详情失败", e);
            redirectAttributes.addFlashAttribute("error", "获取广告位详情失败: " + e.getMessage());
            return "redirect:/admin/ad-units";
        }
    }
} 