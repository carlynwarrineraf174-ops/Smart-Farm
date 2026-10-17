package ui;

import exception.*;
import farm.FarmManager;
import farm.Market;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.enums.PartnerType;
import model.enums.Season;
import model.land.*;
import model.life.House;
import model.life.Vehicle;
import model.livestock.*;
import util.SaveLoad;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FarmView {
    private final Stage stage;
    private FarmManager farm;
    private Market market;

    // Controls
    private Label lblWeek, lblSeason, lblGold, lblDebt, lblAsset, lblHouse, lblVehicle;
    private Label lblLandTitle, lblLivestockTitle, lblNewbornTitle;
    private ListView<Land> landList;
    private ListView<Livestock> livestockList;
    private ListView<Livestock> newbornList;
    private Label lblWarehouse;
    private TextArea logArea;
    private ObservableList<Land> landItems;
    private ObservableList<Livestock> livestockItems;
    private ObservableList<Livestock> newbornItems;

    public FarmView(Stage stage) {
        this.stage = stage;
        this.farm = FarmManager.getInstance();
        this.market = Market.getInstance();
    }

    public BorderPane getRoot() {
        BorderPane root = new BorderPane();
        root.setTop(buildStatusBar());
        root.setCenter(buildContent());
        root.setBottom(buildButtonGrid());
        return root;
    }

    // ========== Status Bar ==========
    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("status-bar");

        lblWeek = new Label("第 1 周");
        lblSeason = new Label("春季");
        lblGold = new Label("金币: 500");
        lblDebt = new Label("负债: 0");
        lblAsset = new Label("资产: 2000");
        lblHouse = new Label("破旧木屋");
        lblVehicle = new Label("破旧自行车");

        for (Label lbl : new Label[]{lblWeek, lblSeason, lblGold, lblDebt, lblAsset, lblHouse, lblVehicle}) {
            lbl.getStyleClass().add("status-value");
            lbl.setMinWidth(Label.USE_PREF_SIZE);
        }

        bar.getChildren().addAll(
                label("📅"), lblWeek,
                label("🌤"), lblSeason,
                label("💰"), lblGold,
                label("🏦"), lblDebt,
                label("📊"), lblAsset,
                label("🏠"), lblHouse,
                label("🚗"), lblVehicle
        );
        return bar;
    }

    private Label label(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("status-label");
        return l;
    }

    // ========== Content ==========
    private VBox buildContent() {
        VBox content = new VBox();
        content.getStyleClass().add("content-panel");

        // Top half: land + livestock + newborns + warehouse
        HBox topHalf = new HBox(8);
        topHalf.setPrefHeight(320);

        // Left: Land list
        VBox left = new VBox(3);
        left.setPrefWidth(350);
        lblLandTitle = title("🌱 土地列表");
        left.getChildren().add(lblLandTitle);
        landItems = FXCollections.observableArrayList();
        landList = new ListView<>(landItems);
        landList.getStyleClass().add("farm-list");
        landList.setCellFactory(param -> new ListCell<Land>() {
            @Override
            protected void updateItem(Land item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "  " + item.toString());
            }
        });
        VBox.setVgrow(landList, Priority.ALWAYS);
        left.getChildren().add(landList);

        // Livestock
        VBox mid = new VBox(3);
        mid.setPrefWidth(230);
        lblLivestockTitle = title("🐄 牲畜圈");
        mid.getChildren().add(lblLivestockTitle);
        livestockItems = FXCollections.observableArrayList();
        livestockList = new ListView<>(livestockItems);
        livestockList.getStyleClass().add("farm-list");
        livestockList.setCellFactory(param -> new ListCell<Livestock>() {
            @Override
            protected void updateItem(Livestock item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "  " + item.toString());
            }
        });
        VBox.setVgrow(livestockList, Priority.ALWAYS);
        mid.getChildren().add(livestockList);

        // Newborns
        VBox newbornCol = new VBox(3);
        newbornCol.setPrefWidth(230);
        lblNewbornTitle = title("🐣 新生栏");
        newbornCol.getChildren().add(lblNewbornTitle);
        newbornItems = FXCollections.observableArrayList();
        newbornList = new ListView<>(newbornItems);
        newbornList.getStyleClass().add("farm-list");
        newbornList.setCellFactory(param -> new ListCell<Livestock>() {
            @Override
            protected void updateItem(Livestock item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "  " + item.toString());
            }
        });
        VBox.setVgrow(newbornList, Priority.ALWAYS);
        newbornCol.getChildren().add(newbornList);

        // Warehouse
        VBox right = new VBox(3);
        right.setPrefWidth(230);
        right.getChildren().add(title("📦 仓库"));
        lblWarehouse = new Label("(空)");
        lblWarehouse.getStyleClass().add("warehouse-label");
        VBox.setVgrow(lblWarehouse, Priority.ALWAYS);
        right.getChildren().add(lblWarehouse);

        topHalf.getChildren().addAll(left, mid, newbornCol, right);

        // Bottom half: Log
        VBox bottom = new VBox(3);
        VBox.setVgrow(bottom, Priority.ALWAYS);
        bottom.getChildren().add(title("📜 消息日志"));
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("log-area");
        logArea.setPrefHeight(160);
        bottom.getChildren().add(logArea);

        content.getChildren().addAll(topHalf, bottom);
        return content;
    }

    private Label title(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("section-title");
        return l;
    }

    // ========== Button Grid ==========
    private GridPane buildButtonGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("button-grid");
        grid.setAlignment(Pos.CENTER);

        String[] labels = {
                "🌱 种植", "🔧 收获", "🏞 买地", "🏪 集市", "🐓 牲畜",
                "🧪 施肥/打药", "🏦 贷款", "⏩ 过一周", "🏡 品质生活", "📈 市场",
                "🏆 成就", "💾 存档", "📂 读档", "🔄 新游戏", "🚪 退出",
                "🌾 一键种植", "🔔 一键收获"
        };
        String[] tips = {
                "在空地上种植作物", "收获已成熟的作物", "购买新的 farmland",
                "在市场买卖商品", "购买、出售或治疗牲畜",
                "使用化肥/猪粪施肥，或喷洒农药", "向银行贷款或还款",
                "推进时间一周", "升级房屋、座驾或寻找伴侣",
                "查看当前市场价格和趋势", "查看已解锁的成就",
                "保存当前游戏进度到文件", "读取之前保存的存档",
                "重新开始游戏（当前进度将丢失）", "退出游戏",
                "在所有空地上自动种上对应作物", "一键收获所有已成熟作物"
        };
        for (int i = 0; i < labels.length; i++) {
            Button btn = new Button(labels[i]);
            btn.getStyleClass().add("action-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setTooltip(new Tooltip(tips[i]));

            int idx = i;
            btn.setOnAction(e -> onButtonClick(idx));

            String cls = labels[i];
            if (cls.contains("退出")) btn.getStyleClass().add("action-btn-danger");
            else if (cls.contains("新游戏")) btn.getStyleClass().add("action-btn-special");
            else if (cls.contains("过一周")) btn.getStyleClass().add("action-btn-special");

            grid.add(btn, i % 5, i / 5);
        }
        // Column constraints — equal width
        for (int col = 0; col < 5; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(20);
            grid.getColumnConstraints().add(cc);
        }
        return grid;
    }

    // ========== Button Handlers ==========
    private void onButtonClick(int idx) {
        try {
            switch (idx) {
                case 0: plantCrop(); break;
                case 1: harvestCrop(); break;
                case 2: buyLand(); break;
                case 3: marketTrade(); break;
                case 4: manageLivestock(); break;
                case 5: useFertilizer(); break;
                case 6: loanManage(); break;
                case 7: passWeek(); break;
                case 8: qualityLife(); break;
                case 9: showMarketReport(); break;
                case 10: showAchievements(); break;
                case 11: saveGame(); break;
                case 12: loadGame(); break;
                case 13: newGame(); break;
                case 14: exitGame(); break;
                case 15: farm.plantAll(); break;
                case 16: farm.harvestAll(); break;
            }
        } catch (Exception ex) {
            logArea.appendText("\n[错误] " + ex.getMessage());
        }
        refresh();
    }

    // ========== Actions ==========

    private void plantCrop() {
        if (farm.getLands().isEmpty()) {
            showError("没有可种植的土地");
            return;
        }
        ChoiceDialog<String> dlg = new ChoiceDialog<>("小麦", "小麦", "玉米", "西红柿");
        dlg.setTitle("种植作物");
        dlg.setHeaderText("选择要种植的作物");
        dlg.setContentText("作物类型：");
        Optional<String> result = dlg.showAndWait();
        if (!result.isPresent()) return;
        String crop = result.get();

        String[] landChoices = new String[farm.getLands().size()];
        for (int i = 0; i < farm.getLands().size(); i++) {
            landChoices[i] = "[" + (i + 1) + "] " + farm.getLands().get(i);
        }
        ChoiceDialog<String> landDlg = new ChoiceDialog<>(landChoices[0], landChoices);
        landDlg.setTitle("选择土地");
        landDlg.setHeaderText("选择种植的土地");
        Optional<String> landResult = landDlg.showAndWait();
        if (!landResult.isPresent()) return;
        int idx = -1;
        for (int i = 0; i < landChoices.length; i++) {
            if (landChoices[i].equals(landResult.get())) { idx = i; break; }
        }
        if (idx < 0) return;

        try {
            farm.plant(idx, crop);
            farm.addMessage("✔ 在土地" + (idx + 1) + "种植了" + crop);
        } catch (Exception e) {
            farm.addMessage("✘ 种植失败: " + e.getMessage());
        }
    }

    private void harvestCrop() {
        java.util.List<String> choices = new java.util.ArrayList<>();
        for (int i = 0; i < farm.getLands().size(); i++) {
            Land land = farm.getLands().get(i);
            if (!land.isEmpty()) {
                choices.add("[" + (i + 1) + "] " + land.toString());
            }
        }
        if (choices.isEmpty()) {
            showError("没有可收获的作物");
            return;
        }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(choices.get(0), choices.toArray(new String[0]));
        dlg.setTitle("收获作物");
        dlg.setHeaderText("选择要收获的土地");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        int idx = -1;
        for (int i = 0; i < farm.getLands().size(); i++) {
            if (!farm.getLands().get(i).isEmpty() && choices.indexOf(r.get()) == i) { idx = i; break; }
        }
        // simpler: find by matching text
        String sel = r.get();
        for (int i = 0; i < farm.getLands().size(); i++) {
            Land land = farm.getLands().get(i);
            String expected = "[" + (i + 1) + "] " + land.toString();
            if (!land.isEmpty() && expected.equals(sel)) { idx = i; break; }
        }
        if (idx < 0) return;

        try {
            int amount = farm.harvest(idx);
            farm.addMessage("✔ 收获成功！获得" + amount + "单位");
        } catch (CropNotMatureException e) {
            farm.addMessage("✘ " + e.getMessage());
        }
    }

    private void buyLand() {
        ChoiceDialog<String> dlg = new ChoiceDialog<>("黑土地 (1000金 适合小麦)",
                "黑土地 (1000金 适合小麦)", "沙地 (900金 适合玉米)", "红壤 (1200金 适合西红柿)");
        dlg.setTitle("购买土地");
        dlg.setHeaderText("选择土地类型");
        dlg.setContentText("类型：");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        String sel = r.get();
        String type = sel.split(" ")[0];
        try {
            farm.buyLand(type);
            farm.addMessage("✔ 购买" + type + "成功！");
        } catch (InsufficientFundsException e) {
            farm.addMessage("✘ " + e.getMessage());
        }
    }

    private void marketTrade() {
        ChoiceDialog<String> dlg = new ChoiceDialog<>("出售", "出售", "购买");
        dlg.setTitle("集市交易");
        dlg.setHeaderText("选择交易类型");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        if ("出售".equals(r.get())) sellGoods();
        else buyGoods();
    }

    private void sellGoods() {
        Map<String, Integer> wh = farm.getWarehouse();
        if (wh.isEmpty()) {
            showError("仓库为空，无物可售");
            return;
        }
        ChoiceDialog<String> modeDlg = new ChoiceDialog<>("选择商品卖出",
                "全部卖出", "选择商品卖出");
        modeDlg.setTitle("出售商品");
        modeDlg.setHeaderText("选择出售方式");
        Optional<String> mode = modeDlg.showAndWait();
        if (!mode.isPresent()) return;

        if ("全部卖出".equals(mode.get())) {
            int totalIncome = 0;
            String[] allItems = wh.keySet().toArray(new String[0]);
            for (String k : allItems) {
                int v = wh.get(k);
                try {
                    farm.sellProduct(k, v);
                    totalIncome += market.getSellPrice(k) * v;
                } catch (Exception ignored) {}
            }
            farm.addMessage("✔ 全部卖出，总收入 " + totalIncome + " 金币");
            return;
        }

        // 选择单个商品
        String[] items = new String[wh.size()];
        int i = 0;
        for (Map.Entry<String, Integer> e : wh.entrySet()) {
            items[i++] = e.getKey() + " x" + e.getValue() + " → 时价 " + market.getCurrentPrice(e.getKey()) + "金";
        }
        ChoiceDialog<String> pickDlg = new ChoiceDialog<>(items[0], items);
        pickDlg.setTitle("选择商品");
        pickDlg.setHeaderText("选择要卖出的商品");
        Optional<String> pick = pickDlg.showAndWait();
        if (!pick.isPresent()) return;
        String itemName = pick.get().split(" ")[0];
        int maxAmount = wh.getOrDefault(itemName, 0);

        TextInputDialog qtyDlg = new TextInputDialog(String.valueOf(maxAmount));
        qtyDlg.setTitle("输入数量");
        qtyDlg.setHeaderText(itemName + " 库存 " + maxAmount + " 个，时价 " + market.getCurrentPrice(itemName) + "金");
        qtyDlg.setContentText("数量（输入 " + maxAmount + " 或直接回车卖完）：");
        Optional<String> qty = qtyDlg.showAndWait();
        if (!qty.isPresent()) return;
        int amount;
        try { amount = Integer.parseInt(qty.get().trim()); } catch (Exception e) { amount = maxAmount; }
        amount = Math.min(amount, maxAmount);
        if (amount <= 0) return;
        try {
            farm.sellProduct(itemName, amount);
            farm.addMessage("✔ 出售" + itemName + " x" + amount + " 成功");
        } catch (Exception e) {
            farm.addMessage("✘ 出售失败: " + e.getMessage());
        }
    }

    private void buyGoods() {
        int used = farm.getUsedStorage();
        int max = farm.getMaxStorage();
        if (used >= max) {
            showError("仓库已满，无法购买更多物资");
            return;
        }

        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("购买物资");
        String[] seeds = {"小麦", "玉米", "西红柿"};
        String[] supplies = {"化肥", "农药", "兽药"};
        String header = "仓库: " + used + "/" + max;
        if (used >= max) header += " [仓库已满！]";
        header += "\n种子: ";
        for (String s : seeds) header += s + market.getBuyPrice(s) + " | ";
        header = header.substring(0, header.length() - 2) + "\n物资: ";
        for (String s : supplies) header += s + market.getBuyPrice(s) + " | ";
        header = header.substring(0, header.length() - 2);
        dlg.setHeaderText(header);
        dlg.setContentText("输入格式: 商品名 数量 (如: 化肥 3)");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        String[] parts = r.get().trim().split("\\s+");
        if (parts.length < 2) { farm.addMessage("✘ 格式错误"); return; }
        try {
            int amount = Integer.parseInt(parts[1]);
            farm.buyItem(parts[0], amount);
            farm.addMessage("✔ 购买" + parts[0] + " x" + amount + " 成功");
        } catch (InsufficientFundsException e) {
            farm.addMessage("✘ " + e.getMessage());
        }
    }
    private void manageLivestock() {
        ChoiceDialog<String> dlg = new ChoiceDialog<>("购买牲畜", "购买牲畜", "查看详情", "出售牲畜", "治疗");
        dlg.setTitle("牲畜管理");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        String sel = r.get();
        if ("购买牲畜".equals(sel)) buyLivestock();
        else if ("出售牲畜".equals(sel)) sellLivestock();
        else if ("治疗".equals(sel)) cureLivestock();
        else showLivestockDetail();
    }


    private void sellLivestock() {
        if (farm.getLivestockList().isEmpty()) {
            showError("没有牲畜可出售");
            return;
        }
        ListView<String> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        int[] indices = new int[farm.getLivestockList().size()];
        for (int i = 0; i < farm.getLivestockList().size(); i++) {
            Livestock a = farm.getLivestockList().get(i);
            int basePrice = market.getSellPrice(a.getName());
            if (basePrice <= 0) basePrice = a.getPurchasePrice() / 2;
            int adjPrice = Math.max(1, (int) (basePrice * a.getAgeSellMultiplier()));
            listView.getItems().add("[" + (i + 1) + "] " + a.getName() + " → " + adjPrice + "金 [" + a.getAgeStage() + "]");
            indices[i] = i;
        }
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("出售牲畜");
        dialog.setHeaderText("选择要出售的牲畜（可多选: Ctrl+Click 或 Shift+Click）");
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? listView.getSelectionModel().getSelectedIndices() : null);
        Optional<List<Integer>> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().isEmpty()) return;

        int sold = 0;
        int income = 0;
        List<Integer> selected = new java.util.ArrayList<>(result.get());
        java.util.Collections.sort(selected, java.util.Collections.reverseOrder());
        for (int idx : selected) {
            income += farm.sellLivestock(idx);
            sold++;
        }
        farm.addMessage("✔ 出售" + sold + "头牲畜，获得约" + income + "金币");
    }

    private void cureLivestock() {
        List<Livestock> sickOnes = new java.util.ArrayList<>();
        for (int i = 0; i < farm.getLivestockList().size(); i++) {
            Livestock a = farm.getLivestockList().get(i);
            if (a.isSick()) sickOnes.add(a);
        }
        if (sickOnes.isEmpty()) {
            showError("没有生病的牲畜");
            return;
        }
        int medicine = farm.getWarehouse().getOrDefault("兽药", 0);
        if (medicine <= 0) {
            showError("兽药库存不足");
            return;
        }
        ListView<String> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (int i = 0; i < farm.getLivestockList().size(); i++) {
            Livestock a = farm.getLivestockList().get(i);
            listView.getItems().add("[" + (i + 1) + "] " + a.getName()
                    + (a.isSick() ? " [生病]" : "")
                    + " (已养" + a.getOwnedWeeks() + "周)");
        }
        // 默认选中生病的
        for (int i = 0; i < farm.getLivestockList().size(); i++) {
            if (farm.getLivestockList().get(i).isSick()) listView.getSelectionModel().select(i);
        }
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("治疗牲畜");
        dialog.setHeaderText("Ctrl+Click 多选，Shift+Click 连选（兽药剩余: " + medicine + "）");
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? listView.getSelectionModel().getSelectedIndices() : null);
        Optional<List<Integer>> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().isEmpty()) return;

        int cured = 0;
        for (int idx : result.get()) {
            try {
                farm.cureLivestock(idx);
                cured++;
            } catch (Exception ignored) {}
        }
        farm.addMessage("✔ 治疗" + cured + "头牲畜成功");
    }

    private void buyLivestock() {
        int chickenPrice = market.getBuyPrice("鸡");
        int pigPrice = market.getBuyPrice("猪");
        int cowPrice = market.getBuyPrice("牛");
        if (chickenPrice <= 0) chickenPrice = 150;
        if (pigPrice <= 0) pigPrice = 600;
        if (cowPrice <= 0) cowPrice = 1500;

        ChoiceDialog<String> dlg = new ChoiceDialog<>("鸡 (" + chickenPrice + "金 消耗:小麦)",
                "鸡 (" + chickenPrice + "金 消耗:小麦)", "猪 (" + pigPrice + "金 消耗:玉米)", "牛 (" + cowPrice + "金 消耗:小麦+玉米)");
        dlg.setTitle("购买牲畜");
        int slots = farm.getLivestockCount();
        int maxSlots = 5 + farm.getHouse().getLevel();
        dlg.setHeaderText("当前市场价  圈舍: " + slots + "/" + maxSlots + "\n"
                + "鸡 " + chickenPrice + "金 (产蛋, 消耗小麦)\n"
                + "猪 " + pigPrice + "金 (产粪, 消耗玉米)\n"
                + "牛 " + cowPrice + "金 (产奶, 消耗小麦+玉米)");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        String type = r.get().split(" ")[0];
        int unitPrice = type.equals("鸡") ? chickenPrice : (type.equals("猪") ? pigPrice : cowPrice);
        int available = maxSlots - slots;
        if (available <= 0) { showError("圈舍已满"); return; }

        TextInputDialog qtyDlg = new TextInputDialog("1");
        qtyDlg.setTitle("购买数量");
        qtyDlg.setHeaderText(type + " × " + unitPrice + "金/头  最多可买" + available + "头  总价: " + (unitPrice * available) + "金");
        qtyDlg.setContentText("数量（1~" + available + "）：");
        Optional<String> qty = qtyDlg.showAndWait();
        if (!qty.isPresent()) return;
        int count;
        try { count = Integer.parseInt(qty.get().trim()); } catch (Exception e) { return; }
        count = Math.min(count, available);
        if (count <= 0) return;

        int bought = 0;
        for (int i = 0; i < count; i++) {
            try {
                farm.buyLivestock(type);
                bought++;
            } catch (Exception e) {
                farm.addMessage("✘ " + e.getMessage());
                break;
            }
        }
        if (bought > 0) farm.addMessage("✔ 成功购买" + bought + "头" + type);
    }

    private void showLivestockDetail() {
        StringBuilder sb = new StringBuilder();
        if (farm.getLivestockList().isEmpty()) {
            sb.append("无牲畜");
        } else {
            for (Livestock a : farm.getLivestockList()) {
                int basePrice = market.getSellPrice(a.getName());
                if (basePrice <= 0) basePrice = a.getPurchasePrice();
                int adjPrice = Math.max(1, (int) (basePrice * a.getAgeSellMultiplier()));
                sb.append("• ").append(a.getName())
                        .append(" 已养").append(a.getOwnedWeeks()).append("/").append(a.getLifespan()).append("周")
                        .append(" ").append(a.getAgeStage()).append(" 时价").append(adjPrice).append("金")
                        .append(" 消耗:");
                for (Map.Entry<String, Integer> feed : a.getWeeklyConsumption().entrySet()) {
                    sb.append(feed.getKey()).append("x").append(feed.getValue()).append(" ");
                }
                sb.append(a.isMature() ? " [已成熟]" : " [成长期]")
                        .append(a.isSick() ? " [生病]" : "")
                        .append(a.getTimesSick() > 0 ? " 生病" + a.getTimesSick() + "/3次" : "").append("\n");
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        alert.setTitle("牲畜详情");
        alert.showAndWait();
    }

    private void useFertilizer() {
        // 第1步：选择物资类型
        ChoiceDialog<String> typeDlg = new ChoiceDialog<>("化肥",
                "化肥 (" + market.getBuyPrice("化肥") + "金 +20%产量)",
                "猪粪 (" + market.getBuyPrice("猪粪") + "金 +10%产量)",
                "农药 (" + market.getBuyPrice("农药") + "金 加速生长3周)");
        int whFert = farm.getWarehouse().getOrDefault("化肥", 0);
        int whManure = farm.getWarehouse().getOrDefault("猪粪", 0);
        int whPest = farm.getWarehouse().getOrDefault("农药", 0);
        typeDlg.setTitle("施肥/打药");
        typeDlg.setHeaderText("仓库: 化肥×" + whFert + "  猪粪×" + whManure + "  农药×" + whPest);
        Optional<String> tr = typeDlg.showAndWait();
        if (!tr.isPresent()) return;
        String typeChoice = tr.get();
        int type = typeChoice.startsWith("化肥") ? 1 : (typeChoice.startsWith("猪粪") ? 2 : 0);
        String itemName = type == 1 ? "化肥" : (type == 2 ? "猪粪" : "农药");
        int stock = type == 1 ? whFert : (type == 2 ? whManure : whPest);
        if (stock <= 0) { showError(itemName + "库存不足"); return; }

        // 第2步：多选土地
        ListView<String> landView = new ListView<>();
        landView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (int i = 0; i < farm.getLands().size(); i++) {
            landView.getItems().add("[" + (i + 1) + "] " + farm.getLands().get(i));
        }
        // 默认选中所有适用的土地
        for (int i = 0; i < farm.getLands().size(); i++) {
            Land land = farm.getLands().get(i);
            if (type > 0 && !land.hasFertilizer()) landView.getSelectionModel().select(i);
            else if (type == 0 && !land.hasPesticide()) landView.getSelectionModel().select(i);
        }

        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("选择土地（可多选）");
        dialog.setHeaderText("Ctrl+Click 多选，Shift+Click 连选\n选择要" + (type > 0 ? "施肥" : "打药") + "的土地（库存: " + itemName + "×" + stock + "）");
        dialog.getDialogPane().setContent(landView);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? landView.getSelectionModel().getSelectedIndices() : null);
        Optional<List<Integer>> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().isEmpty()) return;

        // 第3步：批量操作
        int success = 0, skipped = 0;
        for (int idx : result.get()) {
            try {
                if (type > 0) farm.applyFertilizer(idx, type);
                else farm.applyPesticide(idx);
                success++;
            } catch (Exception e) { skipped++; }
        }
        farm.addMessage("✔ " + itemName + "操作完成：" + success + "块成功"
                + (skipped > 0 ? "，" + skipped + "块跳过" : ""));
    }

    private void loanManage() {
        ChoiceDialog<String> dlg = new ChoiceDialog<>("贷款", "贷款", "还款");
        dlg.setTitle("银行贷款 (当前负债: " + farm.getLoanAmount() + ")");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;

        TextInputDialog input = new TextInputDialog();
        input.setTitle("金额");
        input.setContentText("金额：");
        Optional<String> amt = input.showAndWait();
        if (!amt.isPresent()) return;
        int amount;
        try { amount = Integer.parseInt(amt.get().trim()); } catch (Exception e) { return; }

        if ("贷款".equals(r.get())) {
            farm.takeLoan(amount);
            farm.addMessage("✔ 贷款" + amount + "金币成功");
        } else {
            try {
                farm.repayLoan(amount);
                farm.addMessage("✔ 还款" + amount + "金币成功");
            } catch (InsufficientFundsException e) {
                farm.addMessage("✘ " + e.getMessage());
            }
        }
    }

    private void passWeek() {
        logArea.clear();
        farm.addMessage("--- 第 " + farm.getWeek() + " 周 · " + farm.getCurrentSeason().getDisplayName() + " ---");
        boolean bankrupt = farm.nextWeek();

        List<String> msgs = farm.getAndClearMessages();
        for (String m : msgs) logArea.appendText(m + "\n");

        if (bankrupt) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "你的农场已经资不抵债，无法继续经营。\n\n"
                            + "最终成就：" + farm.getHouse().getName() + " | " + farm.getVehicle().getName() + "\n"
                            + "经营周数：第 " + farm.getWeek() + " 周",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("💀 破产宣告");
            alert.setHeaderText("农场破产！");
            Optional<ButtonType> choice = alert.showAndWait();
            if (choice.isPresent() && choice.get() == ButtonType.YES) {
                FarmManager.resetInstance();
                farm = FarmManager.getInstance();
                market = Market.getInstance();
                market.updatePrices(farm.getCurrentSeason());
                farm.addMessage("新游戏已开始！");
                refresh();
            } else {
                Platform.exit();
            }
        }
        refresh();
    }

    private void qualityLife() {
        while (true) {
            StringBuilder info = new StringBuilder();
            info.append("当前：").append(farm.getHouse().getName())
                    .append(" | ").append(farm.getVehicle().getName())
                    .append(farm.getPartner() != null ? " | " + farm.getPartner().getName() : " | 单身");

            java.util.List<String> optList = new java.util.ArrayList<>();
            House nextH = House.nextLevel(farm.getHouse());
            if (nextH != null) optList.add("升级房产 → " + nextH.getName() + " (" + nextH.getPrice() + "金)");
            else optList.add("房产已满级");
            Vehicle nextV = Vehicle.nextLevel(farm.getVehicle());
            if (nextV != null) optList.add("购买座驾 → " + nextV.getName() + " (" + nextV.getPrice() + "金)");
            else optList.add("座驾已满级");
            if (farm.getPartner() == null)
                optList.add("寻找伴侣 (需庄园+总资产≥30000)");
            else
                optList.add("已有伴侣: " + farm.getPartner().getName());
            optList.add("返回");

            ChoiceDialog<String> dlg = new ChoiceDialog<>(optList.get(0), optList.toArray(new String[0]));
            dlg.setTitle("品质生活");
            dlg.setHeaderText(info.toString());
            Optional<String> r = dlg.showAndWait();
            if (!r.isPresent() || "返回".equals(r.get())) break;

            String sel = r.get();
            if (sel.startsWith("升级房产")) {
                try {
                    if (farm.upgradeHouse()) farm.addMessage("✔ 升级成功！当前房产: " + farm.getHouse().getName());
                } catch (InsufficientFundsException e) { farm.addMessage("✘ " + e.getMessage()); }
            } else if (sel.startsWith("购买座驾")) {
                try {
                    if (farm.upgradeVehicle()) farm.addMessage("✔ 购买成功！当前座驾: " + farm.getVehicle().getName());
                } catch (InsufficientFundsException e) { farm.addMessage("✘ " + e.getMessage()); }
            } else if (sel.startsWith("寻找伴侣")) {
                seekPartnerDialog();
            }
            refresh();
        }
    }

    private void seekPartnerDialog() {
        if (farm.getHouse().getLevel() < 4) { showError("需要豪华庄园才能求婚"); return; }
        if (farm.getTotalAsset() < 30000) { showError("总资产不足30000"); return; }

        String[] opts = new String[PartnerType.values().length];
        for (int i = 0; i < PartnerType.values().length; i++) {
            PartnerType pt = PartnerType.values()[i];
            opts[i] = pt.getName() + " (" + pt.getSkill() + ": " + pt.getDescription() + ")";
        }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(opts[0], opts);
        dlg.setTitle("寻找伴侣");
        dlg.setHeaderText("选择伴侣类型：");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        int idx = -1;
        for (int i = 0; i < opts.length; i++) { if (opts[i].equals(r.get())) { idx = i; break; } }
        try {
            farm.seekPartner(idx);
            farm.addMessage("💑 恭喜！与" + farm.getPartner().getName() + "结为伴侣！");
        } catch (Exception e) {
            farm.addMessage("✘ " + e.getMessage());
        }
    }

    private void showMarketReport() {
        Season s = farm.getCurrentSeason();
        StringBuilder sb = new StringBuilder();

        // 表头
        sb.append(padRight("物品", 10)).append(" ");
        sb.append(padRight("买入", 6)).append(" ");
        sb.append(padRight("卖出", 6)).append(" ");
        sb.append(padRight("回本", 6)).append(" ");
        sb.append(padRight("偏离", 6)).append(" ");
        sb.append(padRight("季节", 10)).append("\n");

        for (int i = 0; i < 48; i++) sb.append('─');
        sb.append('\n');

        String[] items = {"小麦", "玉米", "西红柿", "鸡蛋", "猪粪", "牛奶", "鸡", "猪", "牛"};
        for (String item : items) {
            if (market.getBasePrice(item) > 0) {
                double margin = market.getMargin(item);
                String pct = (int)(margin * 100) + "%";
                String seasonHint = market.getSeasonHint(item, s);

                sb.append(padRight(item, 10)).append(" ");
                sb.append(padRight(String.valueOf(market.getBuyPrice(item)), 6)).append(" ");
                sb.append(padRight(String.valueOf(market.getSellPrice(item)), 6)).append(" ");
                sb.append(padRight(pct, 6)).append(" ");
                sb.append(padRight(market.getFluctuationPercent(item), 6)).append(" ");
                sb.append(padRight(seasonHint, 10)).append("\n");
            }
        }

        sb.append("\n💡 回本比 = 卖出价÷买入价（商品固定值，越高卖越赚）\n");
        sb.append("   偏离 = 当前价比基准价的波动（正→高价适合卖，负→低价适合买）\n");
        sb.append("   🔥旺季涨价 ❄️淡季跌价 → 价格平稳（季节影响长期价格走势）");

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        textArea.setPrefRowCount(16);
        textArea.setPrefColumnCount(50);
        textArea.setPrefWidth(580);
        textArea.setPrefHeight(400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📈 市场报告");
        alert.setHeaderText("📊 本周市场 · " + s.getDisplayName());
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(620, 500);
        alert.showAndWait();
    }

    /** 可视宽度：CJK 汉字算 2，ASCII 算 1 */
    private static int visualWidth(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '一' && c <= '鿿') w += 2;      // CJK 统一表意文字
            else if (c >= '＀' && c <= '￯') w += 2;  // 全角符号
            else if (c >= '　' && c <= '〿') w += 2;  // CJK 符号
            else w += 1;
        }
        return w;
    }

    /** 按可视宽度右填充空格，使总宽度达到 totalWidth */
    private static String padRight(String s, int totalWidth) {
        int need = totalWidth - visualWidth(s);
        if (need <= 0) return s;
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < need; i++) sb.append(' ');
        return sb.toString();
    }

    private void showAchievements() {
        StringBuilder sb = new StringBuilder();
        if (farm.getAchievements().isEmpty()) {
            sb.append("尚未解锁任何成就");
        } else {
            for (String a : farm.getAchievements()) {
                sb.append("• ").append(a).append("\n");
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        alert.setTitle("🏆 成就列表");
        alert.showAndWait();
    }

    private void saveGame() {
        TextInputDialog dlg = new TextInputDialog("存档1");
        dlg.setTitle("保存游戏");
        dlg.setHeaderText("输入存档名称");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        try {
            SaveLoad.save(r.get().trim());
            farm.addMessage("✔ 存档成功: " + r.get());
        } catch (Exception e) {
            farm.addMessage("✘ 存档失败: " + e.getMessage());
        }
    }

    private void loadGame() {
        String[] saves = SaveLoad.listSaves();
        if (saves.length == 0) { showError("没有找到存档"); return; }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(saves[0].replace(".txt", ""),
                java.util.Arrays.stream(saves).map(s -> s.replace(".txt", "")).toArray(String[]::new));
        dlg.setTitle("读取存档");
        dlg.setHeaderText("选择存档");
        Optional<String> r = dlg.showAndWait();
        if (!r.isPresent()) return;
        try {
            SaveLoad.load(r.get());
            farm = FarmManager.getInstance();
            market = Market.getInstance();
            farm.addMessage("✔ 读取存档成功: " + r.get());
            refresh();
        } catch (Exception e) {
            farm.addMessage("✘ 读取失败: " + e.getMessage());
        }
    }

    public void showStartDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "欢迎来到智慧农场！\n一款农场经营模拟游戏。",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("🌾 智慧农场");
        alert.setHeaderText("开始新游戏？");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.YES)).setText("新游戏");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.NO)).setText("读取存档");
        Optional<ButtonType> r = alert.showAndWait();

        if (r.isPresent() && r.get() == ButtonType.YES) {
            FarmManager.resetInstance();
            farm = FarmManager.getInstance();
            market = Market.getInstance();
            market.updatePrices(farm.getCurrentSeason());
            farm.addMessage("🌾 欢迎来到智慧农场！");
            refresh();
        } else {
            loadGame();
        }
    }

    private void newGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要开始新游戏吗？当前进度将丢失。", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        FarmManager.resetInstance();
        farm = FarmManager.getInstance();
        market = Market.getInstance();
        market.updatePrices(farm.getCurrentSeason());
        logArea.clear();
        farm.addMessage("🌾 新游戏开始！");
        refresh();
    }

    private void exitGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要退出吗？", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            Platform.exit();
        }
    }

    // 大数字缩略显示（万以上显示 X.X万）
    private String fmt(int n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setTitle("提示");
        alert.showAndWait();
    }

    // ========== Refresh UI ==========
    private void refresh() {
        // Status bar
        lblWeek.setText("第 " + farm.getWeek() + " 周");
        lblSeason.setText(farm.getCurrentSeason().getDisplayName()
                + " (" + farm.getWeeksInSeason() + "/4周)");
        lblGold.setText("金币: " + fmt(farm.getGold()));
        lblGold.setTooltip(new Tooltip(String.valueOf(farm.getGold())));
        lblDebt.setText("负债: " + fmt(farm.getLoanAmount()));
        lblDebt.setTooltip(new Tooltip(String.valueOf(farm.getLoanAmount())));
        lblAsset.setText("资产: " + fmt(farm.getTotalAsset()));
        lblAsset.setTooltip(new Tooltip(String.valueOf(farm.getTotalAsset())));
        lblHouse.setText(farm.getHouse().getName());
        lblVehicle.setText(farm.getVehicle().getName());

        // Land list
        landItems.setAll(farm.getLands());
        int black = 0, sandy = 0, red = 0;
        for (Land land : farm.getLands()) {
            if (land instanceof BlackSoilLand) black++;
            else if (land instanceof SandyLand) sandy++;
            else if (land instanceof RedSoilLand) red++;
        }
        StringBuilder landInfo = new StringBuilder("🌱 土地列表 (" + farm.getLands().size() + "块");
        if (black > 0) landInfo.append(" 黑土" + black);
        if (sandy > 0) landInfo.append(" 沙地" + sandy);
        if (red > 0) landInfo.append(" 红壤" + red);
        landInfo.append(")");
        lblLandTitle.setText(landInfo.toString());

        // Livestock list
        livestockItems.setAll(farm.getLivestockList());
        int totalSlots = 5 + farm.getHouse().getLevel();
        int chickenCnt = 0, pigCnt = 0, cowCnt = 0;
        for (Livestock a : farm.getLivestockList()) {
            if (a instanceof Chicken) chickenCnt++;
            else if (a instanceof Pig) pigCnt++;
            else if (a instanceof Cow) cowCnt++;
        }
        StringBuilder liveInfo = new StringBuilder("🐄 牲畜圈 (" + farm.getLivestockCount() + "/" + totalSlots + "头");
        if (chickenCnt > 0) liveInfo.append(" 鸡" + chickenCnt);
        if (pigCnt > 0) liveInfo.append(" 猪" + pigCnt);
        if (cowCnt > 0) liveInfo.append(" 牛" + cowCnt);
        liveInfo.append(")");
        lblLivestockTitle.setText(liveInfo.toString());

        // Newborn list
        List<Livestock> nb = farm.getNewborns();
        newbornItems.setAll(nb);
        int nbChicken = 0, nbPig = 0, nbCow = 0;
        for (Livestock a : nb) {
            if (a instanceof Chicken) nbChicken++;
            else if (a instanceof Pig) nbPig++;
            else if (a instanceof Cow) nbCow++;
        }
        StringBuilder nbInfo = new StringBuilder("🐣 新生栏 (共" + nb.size() + "头");
        if (nbChicken > 0) nbInfo.append(" 鸡" + nbChicken);
        if (nbPig > 0) nbInfo.append(" 猪" + nbPig);
        if (nbCow > 0) nbInfo.append(" 牛" + nbCow);
        nbInfo.append(")");
        lblNewbornTitle.setText(nbInfo.toString());

        // Warehouse
        Map<String, Integer> wh = farm.getWarehouse();
        StringBuilder sb = new StringBuilder();
        sb.append("── 作物 ──\n");
        sb.append("小麦  x").append(wh.getOrDefault("小麦", 0)).append("\n");
        sb.append("玉米  x").append(wh.getOrDefault("玉米", 0)).append("\n");
        sb.append("西红柿  x").append(wh.getOrDefault("西红柿", 0)).append("\n");
        sb.append("── 畜产品 ──\n");
        sb.append("鸡蛋  x").append(wh.getOrDefault("鸡蛋", 0)).append("\n");
        sb.append("猪粪  x").append(wh.getOrDefault("猪粪", 0)).append("\n");
        sb.append("牛奶  x").append(wh.getOrDefault("牛奶", 0)).append("\n");
        sb.append("── 消耗品 ──\n");
        sb.append("化肥  x").append(wh.getOrDefault("化肥", 0)).append("\n");
        sb.append("农药  x").append(wh.getOrDefault("农药", 0)).append("\n");
        sb.append("兽药  x").append(wh.getOrDefault("兽药", 0)).append("\n");
        sb.append("容量: ").append(farm.getUsedStorage()).append("/").append(farm.getMaxStorage());
        lblWarehouse.setText(sb.toString());

        // Log messages
        List<String> msgs = farm.getAndClearMessages();
        for (String m : msgs) {
            logArea.appendText(m + "\n");
        }
    }
}
