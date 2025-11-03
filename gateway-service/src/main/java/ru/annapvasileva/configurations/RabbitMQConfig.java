package ru.annapvasileva.configurations;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String CAT_EXCHANGE = "cat.exchange";
    public static final String OWNER_EXCHANGE = "owner.exchange";

    public static final String CAT_CREATE_QUEUE = "cat.create.queue";
    public static final String CAT_GET_QUEUE = "cat.get.queue";
    public static final String CAT_GETALL_QUEUE = "cat.getAll.queue";
    public static final String CAT_MAKEFRIENDS_QUEUE = "cat.makeFriends.queue";
    public static final String CAT_DELETEFRIENDSHIP_QUEUE = "cat.deleteFriendship.queue";
    public static final String CAT_DELETE_QUEUE = "cat.delete.queue";
    public static final String CAT_UPDATE_QUEUE = "cat.update.queue";
    public static final String OWNER_CREATE_QUEUE = "owner.create.queue";
    public static final String OWNER_GET_QUEUE = "owner.get.queue";
    public static final String OWNER_GETALL_QUEUE = "owner.getAll.queue";
    public static final String OWNER_CHANGEFORPET_QUEUE = "owner.changeForPet.queue";
    public static final String OWNER_DELETEPET_QUEUE = "owner.deletePet.queue";
    public static final String OWNER_DELETE_QUEUE = "owner.delete.queue";
    public static final String OWNER_UPDATE_QUEUE = "owner.update.queue";

    private Queue queueWithDlq(String name, String dlqName) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", dlqName);
        return new Queue(name, true, false, false, args);
    }

    @Bean
    public TopicExchange catExchange() {
        return new TopicExchange(CAT_EXCHANGE);
    }

    @Bean
    public TopicExchange ownerExchange() {
        return new TopicExchange(OWNER_EXCHANGE);
    }

    @Bean
    public Queue catCreateQueue() {
        return queueWithDlq(CAT_CREATE_QUEUE, "cat.dlq");
    }

    @Bean
    public Queue catGetQueue() {
        return queueWithDlq(CAT_GET_QUEUE, "cat.dlq");
    }

    @Bean
    public Queue catGetAllQueue() {
        return queueWithDlq(CAT_GETALL_QUEUE, "cat.dlq");
    }

    @Bean
    public Queue catMakeFriendsQueue() {
        return queueWithDlq(CAT_MAKEFRIENDS_QUEUE, "cat.dlq");
    }

    @Bean
    public Queue catDeleteFriendshipQueue() {
        return queueWithDlq(CAT_DELETEFRIENDSHIP_QUEUE, "cat.dlq");
    }

    @Bean
    public Queue catDeleteQueue() {
        return queueWithDlq(CAT_DELETE_QUEUE, "cat.dlq");
    }

    @Bean
    public Queue catUpdateQueue() {
        return queueWithDlq(CAT_UPDATE_QUEUE, "cat.dlq");
    }

    @Bean
    public Queue ownerCreateQueue() {
        return queueWithDlq(OWNER_CREATE_QUEUE, "owner.dlq");
    }

    @Bean
    public Queue ownerGetQueue() {
        return queueWithDlq(OWNER_GET_QUEUE, "owner.dlq");
    }

    @Bean
    public Queue ownerGetAllQueue() {
        return queueWithDlq(OWNER_GETALL_QUEUE, "owner.dlq");
    }

    @Bean
    public Queue ownerChangeForPetQueue() {
        return queueWithDlq(OWNER_CHANGEFORPET_QUEUE, "owner.dlq");
    }

    @Bean
    public Queue ownerDeletePetQueue() {
        return queueWithDlq(OWNER_DELETEPET_QUEUE, "owner.dlq");
    }

    @Bean
    public Queue ownerDeleteQueue() {
        return queueWithDlq(OWNER_DELETE_QUEUE, "owner.dlq");
    }

    @Bean
    public Queue ownerUpdateQueue() {
        return queueWithDlq(OWNER_UPDATE_QUEUE, "owner.dlq");
    }

    private Binding binding(Queue queue, TopicExchange exchange, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public Binding catCreateBinding() {
        return binding(catCreateQueue(), catExchange(), "cat.create");
    }

    @Bean
    public Binding catGetBinding() {
        return binding(catGetQueue(), catExchange(), "cat.get");
    }

    @Bean
    public Binding catGetAllBinding() {
        return binding(catGetAllQueue(), catExchange(), "cat.getAll");
    }

    @Bean
    public Binding catMakeFriendsBinding() {
        return binding(catMakeFriendsQueue(), catExchange(), "cat.makeFriends");
    }

    @Bean
    public Binding catDeleteFriendshipBinding() {
        return binding(catDeleteFriendshipQueue(), catExchange(), "cat.deleteFriendship");
    }

    @Bean
    public Binding catDeleteBinding() {
        return binding(catDeleteQueue(), catExchange(), "cat.delete");
    }

    @Bean
    public Binding catUpdateBinding() {
        return binding(catUpdateQueue(), catExchange(), "cat.update");
    }

    @Bean
    public Binding ownerCreateBinding() {
        return binding(ownerCreateQueue(), ownerExchange(), "owner.create");
    }

    @Bean
    public Binding ownerGetBinding() {
        return binding(ownerGetQueue(), ownerExchange(), "owner.get");
    }

    @Bean
    public Binding ownerGetAllBinding() {
        return binding(ownerGetAllQueue(), ownerExchange(), "owner.getAll");
    }

    @Bean
    public Binding ownerChangeForPetBinding() {
        return binding(ownerChangeForPetQueue(), ownerExchange(), "owner.changeForPet");
    }

    @Bean
    public Binding ownerDeletePetBinding() {
        return binding(ownerDeletePetQueue(), ownerExchange(), "owner.deletePet");
    }

    @Bean
    public Binding ownerDeleteBinding() {
        return binding(ownerDeleteQueue(), ownerExchange(), "owner.delete");
    }

    @Bean
    public Binding ownerUpdateBinding() {
        return binding(ownerUpdateQueue(), ownerExchange(), "owner.update");
    }

    @Bean
    public Queue catCreateReplyQueue() {
        return queueWithDlq("cat.create.reply.queue", "cat.dlq");
    }

    @Bean
    public Queue catGetReplyQueue() {
        return queueWithDlq("cat.get.reply.queue", "cat.dlq");
    }

    @Bean
    public Queue catGetAllReplyQueue() {
        return queueWithDlq("cat.getAll.reply.queue", "cat.dlq");
    }

    @Bean
    public Queue catMakeFriendsReplyQueue() {
        return queueWithDlq("cat.makeFriends.reply.queue", "cat.dlq");
    }

    @Bean
    public Queue catDeleteFriendshipReplyQueue() {
        return queueWithDlq("cat.deleteFriendship.reply.queue", "cat.dlq");
    }

    @Bean
    public Queue catDeleteReplyQueue() {
        return queueWithDlq("cat.delete.reply.queue", "cat.dlq");
    }

    @Bean
    public Queue catUpdateReplyQueue() {
        return queueWithDlq("cat.update.reply.queue", "cat.dlq");
    }

    @Bean
    public Queue ownerCreateReplyQueue() {
        return queueWithDlq("owner.create.reply.queue", "owner.dlq");
    }

    @Bean
    public Queue ownerGetReplyQueue() {
        return queueWithDlq("owner.get.reply.queue", "owner.dlq");
    }

    @Bean
    public Queue ownerGetAllReplyQueue() {
        return queueWithDlq("owner.getAll.reply.queue", "owner.dlq");
    }

    @Bean
    public Queue ownerChangeForPetReplyQueue() {
        return queueWithDlq("owner.changeForPet.reply.queue", "owner.dlq");
    }

    @Bean
    public Queue ownerDeletePetReplyQueue() {
        return queueWithDlq("owner.deletePet.reply.queue", "owner.dlq");
    }

    @Bean
    public Queue ownerDeleteReplyQueue() {
        return queueWithDlq("owner.delete.reply.queue", "owner.dlq");
    }

    @Bean
    public Queue ownerUpdateReplyQueue() {
        return queueWithDlq("owner.update.reply.queue", "owner.dlq");
    }

    @Bean
    public Binding catCreateReplyBinding() {
        return binding(catCreateReplyQueue(), catExchange(), "cat.create.reply");
    }

    @Bean
    public Binding catGetReplyBinding() {
        return binding(catGetReplyQueue(), catExchange(), "cat.get.reply");
    }

    @Bean
    public Binding catGetAllReplyBinding() {
        return binding(catGetAllReplyQueue(), catExchange(), "cat.getAll.reply");
    }

    @Bean
    public Binding catMakeFriendsReplyBinding() {
        return binding(catMakeFriendsReplyQueue(), catExchange(), "cat.makeFriends.reply");
    }

    @Bean
    public Binding catDeleteFriendshipReplyBinding() {
        return binding(catDeleteFriendshipReplyQueue(), catExchange(), "cat.deleteFriendship.reply");
    }

    @Bean
    public Binding catDeleteReplyBinding() {
        return binding(catDeleteReplyQueue(), catExchange(), "cat.delete.reply");
    }

    @Bean
    public Binding catUpdateReplyBinding() {
        return binding(catUpdateReplyQueue(), catExchange(), "cat.update.reply");
    }

    @Bean
    public Binding ownerCreateReplyBinding() {
        return binding(ownerCreateReplyQueue(), ownerExchange(), "owner.create.reply");
    }

    @Bean
    public Binding ownerGetReplyBinding() {
        return binding(ownerGetReplyQueue(), ownerExchange(), "owner.get.reply");
    }

    @Bean
    public Binding ownerGetAllReplyBinding() {
        return binding(ownerGetAllReplyQueue(), ownerExchange(), "owner.getAll.reply");
    }

    @Bean
    public Binding ownerChangeForPetReplyBinding() {
        return binding(ownerChangeForPetReplyQueue(), ownerExchange(), "owner.changeForPet.reply");
    }

    @Bean
    public Binding ownerDeletePetReplyBinding() {
        return binding(ownerDeletePetReplyQueue(), ownerExchange(), "owner.deletePet.reply");
    }

    @Bean
    public Binding ownerDeleteReplyBinding() {
        return binding(ownerDeleteReplyQueue(), ownerExchange(), "owner.delete.reply");
    }

    @Bean
    public Binding ownerUpdateReplyBinding() {
        return binding(ownerUpdateReplyQueue(), ownerExchange(), "owner.update.reply");
    }

    @Bean
    public Queue catDlq() {
        return new Queue("cat.dlq", true);
    }

    @Bean
    public Queue ownerDlq() {
        return new Queue("owner.dlq", true);
    }

    @Bean
    public Binding catDlqBinding() {
        return BindingBuilder.bind(catDlq())
                .to(new DirectExchange(""))
                .with("cat.dlq");
    }

    @Bean
    public Binding ownerDlqBinding() {
        return BindingBuilder.bind(ownerDlq())
                .to(new DirectExchange(""))
                .with("owner.dlq");
    }
}
