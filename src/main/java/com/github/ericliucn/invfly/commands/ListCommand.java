package com.github.ericliucn.invfly.commands;

import com.github.ericliucn.invfly.Invfly;
import com.github.ericliucn.invfly.config.Message;
import com.github.ericliucn.invfly.data.StorageData;
import com.github.ericliucn.invfly.managers.DatabaseManager;
import com.github.ericliucn.invfly.service.SyncDataService;
import com.github.ericliucn.invfly.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ListCommand implements CommandExecutor {

    private Message message;
    private DatabaseManager databaseManager;
    private User user;
    private PaginationService paginationService;
    private SyncDataService service;
    private SpongeExecutorService asyncExecutor;
    private List<StorageData> dataList;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        asyncExecutor = Invfly.instance.getAsyncExecutor();
        asyncExecutor.submit(()->{
            paginationService = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
            message = Invfly.instance.getConfigLoader().getMessage();
            databaseManager = Invfly.instance.getDatabaseManager();
            service = Invfly.instance.getService();
            DatabaseManager databaseManager = Invfly.instance.getDatabaseManager();
            PaginationList.Builder builder = paginationService.builder().padding(Utils.toText("&a="));
            Duration defaultDuration = Invfly.instance.getConfigLoader().getConfig().general.outDate;
            args.<User>getOne("user").ifPresent(thisUser -> {
                user = thisUser;
                builder.title(message.getMessage("gui.title").replace("%user%", Utils.toText(user.getName())));
                Duration duration = args.<Duration>getOne("duration").orElse(defaultDuration);
                dataList = databaseManager.getAllData(thisUser, duration);
                builder.contents(getTexts(dataList, user)).sendTo(src);
            });
        });
        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .executor(new ListCommand())
                .permission("invfly.command.list")
                .arguments(
                        GenericArguments.seq(
                                GenericArguments.user(Text.of("user")),
                                GenericArguments.optional(GenericArguments.duration(Text.of("duration")))
                        )
                )
                .build();
    }

    private List<Text> getTexts(List<StorageData> dataList, User user){
        List<Text> texts = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            StorageData data = dataList.get(i);
            Text index = Utils.toText("&b["+ i + "] ");
            Text id = Utils.toText("&a[" + "ID:" + data.getId() + "]");
            String timeStr = new SimpleDateFormat("HH:mm:ss").format(data.getTime());
            Text time = Utils.toText("&e[" + timeStr + "]");
            Text toPlayer = message.getMessage("gui.button.to.user")
                    .toBuilder()
                    .onClick(TextActions.executeCallback(source -> load(data, user, source)))
                    .onHover(TextActions.showText(message.getMessage("gui.button.to.user.tip")))
                    .build();
            Text toMe = message.getMessage("gui.button.to.me")
                    .toBuilder()
                    .onClick(TextActions.executeCallback(source -> load(data, ((User) source), source)))
                    .onHover(TextActions.showText(message.getMessage("gui.button.to.me.tip")))
                    .build();
            Text detail = message.getMessage("gui.button.detail")
                    .toBuilder()
                    .onHover(TextActions.showText(message.getMessage("gui.button.detail.tip")))
                    .onClick(TextActions.executeCallback(source -> showDetail(data, source, user)))
                    .build();
            Text delete = message.getMessage("gui.button.delete")
                    .toBuilder()
                    .onHover(TextActions.showText(message.getMessage("gui.button.delete.tip")))
                    .onClick(TextActions.executeCallback(source -> deleteRecord(data, source, user)))
                    .build();
            Text one = Text.builder().append(index, id, time, toPlayer, toMe, detail, delete).build();
            texts.add(one);
        }
        return texts;
    }

    private void load(StorageData data, User user, CommandSource source){
        asyncExecutor.submit(()->{
            service.loadUserData(user, data, false, source);
        });
    }

    private void showDetail(StorageData data, CommandSource source, User user){
        List<Text> texts = new ArrayList<>();
        Text id = message.getMessage("gui.id")
                .toBuilder()
                .append(Utils.toText("&b " + data.getId()))
                .build();
        Text uuid = message.getMessage("gui.uuid")
                .toBuilder()
                .append(Utils.toText("&b " + data.getUuid()))
                .build();
        Text name = message.getMessage("gui.name")
                .toBuilder()
                .append(Utils.toText("&b " + data.getName()))
                .build();
        Text time = message.getMessage("gui.time")
                .toBuilder()
                .append(Utils.toText("&b " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(data.getTime())))
                .build();
        Text server = message.getMessage("gui.server")
                .toBuilder()
                .append(Utils.toText("&b " + data.getServerName()))
                .build();
        Text isDisconnect = message.getMessage("gui.disconnect")
                .toBuilder()
                .append(Utils.toText("&b " + data.isDisconnect()))
                .build();
        Text recoverToUser = message.getMessage("gui.button.to.user")
                .toBuilder()
                .onClick(TextActions.executeCallback(clickSource -> load(data, user, clickSource)))
                .onHover(TextActions.showText(message.getMessage("gui.button.to.user.tip")))
                .build();
        Text recoverMe = message.getMessage("gui.button.to.me")
                .toBuilder()
                .onClick(TextActions.executeCallback(clickSource -> load(data, ((User) clickSource), clickSource)))
                .onHover(TextActions.showText(message.getMessage("gui.button.to.me.tip")))
                .build();
        Text delete = message.getMessage("gui.button.delete")
                .toBuilder()
                .onClick(TextActions.executeCallback(clickSource -> deleteRecord(data, clickSource, user)))
                .onHover(TextActions.showText(message.getMessage("gui.button.delete.tip")))
                .build();
        Text buttons = Text.builder()
                .append(recoverToUser, recoverMe ,delete)
                .build();
        texts.add(id);
        texts.add(uuid);
        texts.add(name);
        texts.add(time);
        texts.add(server);
        texts.add(isDisconnect);
        paginationService.builder()
                .title(message.getMessage("gui.title").replace("%user%", Utils.toText(user.getName())))
                .padding(Utils.toText("&a=")).contents(texts).footer(buttons)
                .sendTo(source);
    }
    
    private void deleteRecord(StorageData data, CommandSource source, User user){
        asyncExecutor.submit(()->{
            try {
                dataList.remove(data);
                paginationService.builder()
                        .title(message.getMessage("gui.title").replace("%user%", Utils.toText(user.getName())))
                        .padding(Utils.toText("&a="))
                        .contents(getTexts(dataList, user))
                        .sendTo(source);
                databaseManager.deleteRecord(data.getId());
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

}
