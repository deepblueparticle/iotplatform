package org.iotp.server.actors.plugin;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.iotp.analytics.ruleengine.api.device.DeviceAttributesEventNotificationMsg;
import org.iotp.analytics.ruleengine.common.msg.cluster.ServerAddress;
import org.iotp.analytics.ruleengine.plugins.msg.TimeoutMsg;
import org.iotp.analytics.ruleengine.plugins.msg.ToDeviceRpcRequest;
import org.iotp.analytics.ruleengine.plugins.msg.ToDeviceRpcRequestPluginMsg;
import org.iotp.infomgt.dao.asset.AssetService;
import org.iotp.infomgt.dao.attributes.AttributesService;
import org.iotp.infomgt.dao.customer.CustomerService;
import org.iotp.infomgt.dao.device.DeviceService;
import org.iotp.infomgt.dao.plugin.PluginService;
import org.iotp.infomgt.dao.rule.RuleService;
import org.iotp.infomgt.dao.tenant.TenantService;
import org.iotp.infomgt.dao.timeseries.TimeseriesService;
import org.iotp.infomgt.data.id.DeviceId;
import org.iotp.infomgt.data.id.PluginId;
import org.iotp.infomgt.data.id.TenantId;
import org.iotp.server.actors.ActorSystemContext;
import org.iotp.server.controller.plugin.PluginWebSocketMsgEndpoint;
import org.iotp.server.service.cluster.routing.ClusterRoutingService;
import org.iotp.server.service.cluster.rpc.ClusterRpcService;

import akka.actor.ActorRef;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.Duration;

@Slf4j
public final class SharedPluginProcessingContext {
  final ActorRef parentActor;
  final ActorRef currentActor;
  final ActorSystemContext systemContext;
  final PluginWebSocketMsgEndpoint msgEndpoint;
  final AssetService assetService;
  final DeviceService deviceService;
  final RuleService ruleService;
  final PluginService pluginService;
  final CustomerService customerService;
  final TenantService tenantService;
  final TimeseriesService tsService;
  final AttributesService attributesService;
  final ClusterRpcService rpcService;
  final ClusterRoutingService routingService;
  final PluginId pluginId;
  final TenantId tenantId;

  public SharedPluginProcessingContext(ActorSystemContext sysContext, TenantId tenantId, PluginId pluginId,
      ActorRef parentActor, ActorRef self) {
    super();
    this.tenantId = tenantId;
    this.pluginId = pluginId;
    this.parentActor = parentActor;
    this.currentActor = self;
    this.systemContext = sysContext;
    this.msgEndpoint = sysContext.getWsMsgEndpoint();
    this.tsService = sysContext.getTsService();
    this.attributesService = sysContext.getAttributesService();
    this.assetService = sysContext.getAssetService();
    this.deviceService = sysContext.getDeviceService();
    this.rpcService = sysContext.getRpcService();
    this.routingService = sysContext.getRoutingService();
    this.ruleService = sysContext.getRuleService();
    this.pluginService = sysContext.getPluginService();
    this.customerService = sysContext.getCustomerService();
    this.tenantService = sysContext.getTenantService();
  }

  public PluginId getPluginId() {
    return pluginId;
  }

  public TenantId getPluginTenantId() {
    return tenantId;
  }

  public void toDeviceActor(DeviceAttributesEventNotificationMsg msg) {
    forward(msg.getDeviceId(), msg, rpcService::tell);
  }

  public void sendRpcRequest(ToDeviceRpcRequest msg) {
    log.trace("[{}] Forwarding msg {} to device actor!", pluginId, msg);
    ToDeviceRpcRequestPluginMsg rpcMsg = new ToDeviceRpcRequestPluginMsg(pluginId, tenantId, msg);
    forward(msg.getDeviceId(), rpcMsg, rpcService::tell);
  }

  private <T> void forward(DeviceId deviceId, T msg, BiConsumer<ServerAddress, T> rpcFunction) {
    Optional<ServerAddress> instance = routingService.resolveById(deviceId);
    if (instance.isPresent()) {
      log.trace("[{}] Forwarding msg {} to remote device actor!", pluginId, msg);
      rpcFunction.accept(instance.get(), msg);
    } else {
      log.trace("[{}] Forwarding msg {} to local device actor!", pluginId, msg);
      parentActor.tell(msg, ActorRef.noSender());
    }
  }

  public void scheduleTimeoutMsg(TimeoutMsg msg) {
    log.debug("Scheduling msg {} with delay {} ms", msg, msg.getTimeout());
    systemContext.getScheduler().scheduleOnce(Duration.create(msg.getTimeout(), TimeUnit.MILLISECONDS), currentActor,
        msg, systemContext.getActorSystem().dispatcher(), ActorRef.noSender());

  }

  public void persistError(String method, Exception e) {
    systemContext.persistError(tenantId, pluginId, method, e);
  }

  public ActorRef self() {
    return currentActor;
  }
}
