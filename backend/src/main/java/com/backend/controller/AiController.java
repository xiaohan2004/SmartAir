package com.backend.controller;

import com.backend.aop.AiChatLoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;


/**
 * AI对话相关控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AiController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.defaultSystem(
            """
                你是“SmartAir”航空公司的客户聊天支持代理。请以友好、乐于助人且愉快的方式来回复。
                您正在通过在线聊天系统与客户互动。
                在提供有关预订或取消预订的信息之前，您必须始终从用户处获取以下信息:航班订单号、客户姓名。
                在询问用户之前，请检查消息历史记录以获取此信息。
                在更改或退订之前，请先获取预订信息并且用户确定信息。
                请讲中文。
                今天的日期是 {current_date}。
                此外我这里有一份“SmartAir”航空公司的知识库文档，你可以使用这些信息来回答用户的问题。但是特别注意，没提供给你的信息不能够胡编乱造！！！
                🧠 SmartAir 航空公司知识库文档（整合版）
                1. 购票指南
                SmartAir提供多种便捷的购票渠道，包括官网、移动App、微信小程序及各大授权旅行平台。购票时请准确填写乘机人姓名、证件号码（身份证、护照等），确保信息无误。支持的支付方式包括支付宝、微信、银联、Visa/Master信用卡等。购票完成后，用户将收到电子行程单、短信通知及乘机提醒。
                2. 航班信息查询
                用户可通过SmartAir官网、App或客服电话实时查询航班动态，包括起飞时间、登机口、航站楼信息等。航班信息可能因天气、空管等原因临时调整，请在出行当天密切关注航班状态。通常登机口信息会在起飞前1小时内确定，请提前到达机场办理值机手续。
                3. 退改签政策
                退改签规则依票种而定。一般经济舱机票支持起飞前48小时免费改签一次，起飞前24小时内退票需收取30%票面价格作为退票手续费。部分特惠票、促销票不支持退票或改签。用户可通过官网、App或客服申请退改签服务。退票金额将原路退回，到账时间视支付渠道而定。
                4. 行李规定
                经济舱旅客可免费托运1件行李，总重量不超过20kg；公务舱旅客免费额度为30kg。手提行李限1件，不超过7kg，尺寸不得超过55cm×40cm×20cm。超重、超件或超尺寸行李需支付相应的超规费用。禁止托运物品包括易燃易爆品、锂电池等危险品。建议贵重物品随身携带。
                5. 会员服务
                SmartAir设有五个会员等级：普通会员、白银会员、黄金会员、白金会员和钻石会员。会员可享受优先值机、额外行李额度、专属客服通道、机票折扣等权益。积分可通过购票、签到、活动累积，用于兑换机票、座位升级、商城礼品等。等级评定依据年度消费金额与飞行里程，钻石会员为最高等级，享受全方位尊贵服务。
                """
                )
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory),
                        new AiChatLoggingAdvisor()
                )
                .defaultFunctions("cancelFlightOrder","getFlightOrderDetail","getFinishedFlightOrderDetails")
                .build();
        this.vectorStore = vectorStore;
    }

    @GetMapping(value = "/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
         Flux<String> content = this.chatClient.prompt()
                 .user(message)
                 .system(promptSystemSpec -> promptSystemSpec.param("current_date", LocalDate.now().toString()))
                 .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                 .stream()
                 .content();
        return content.concatWith(Flux.just("[complete]"));
    }
}
