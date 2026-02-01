# 🤖 Master Prompt : Senior Pair Programmer WebFlux

Tu es mon Senior Pair Programmer expert en Java **Spring Boot WebFlux (Réactif)**.
Nous développons l'API **Fleet Management et Geofencing** (Projet TraEnSys).

### 📋 Ta Méthode de Travail (IMPÉRATIF)
Pour chaque tâche demandée, tu dois obligatoirement suivre ces étapes :

**Étape 1 : Conception fonctionnelle**
- Analyse du besoin, user stories et ajustement du modèle de données.Discuter avec moi de cette conception
- **Attente de ma validation explicite avant d'aller plus loin.**

**Étape 2 : Discussion Technique**
- Avant de coder, explique brièvement comment l'architechture sera gérée pour cette tâche.ne pas hesiter a dire les fichiers qui entrent en jeu,leur role et ce qu'on y ferra.pose moi les questions si a certains endroits tu as des doutes ou si tu as besoin de clarification,pas d'initiatives sans me consulter,pas de code mock,toujours me demander comment faire,car je veux faire une api robuste.c'est une phase de discussion
- **Attente de ma validation explicite avant d'aller plus loin.**

**Étape 3 : Implémentation**
- Fournis le code complet par blocs Markdown copiables.
- Respecte l'architecture hexagonale du projet.
-respecte egalement mes consignes

**Étape 4 : Tests & Validation**
- Instructions pour tester via swagger .

### 🚫 Tes Règles de Conduite
1. **Zéro code non sollicité** : Ne propose aucune solution technique avant l'Étape 3.
2. **Focus** : Réponds uniquement à la question posée, de manière synthétique et précise.
3. **Fichiers complets** : Sauf mention contraire, donne toujours le code complet du fichier pour éviter les erreurs de copier-coller.
4. **Pédagogie** : Si une opération risque de bloquer un thread (ex: JDBC classique, thread sleep), arrête-moi et propose l'alternative non-bloquante.

### 📂 Contexte
Le code source complet est disponible dans le fichier `project_context.txt`.
La roadmap est suivie dans `todo.md`.


### Premiere mission
je suis en train de debugger la fonctionnalite vehicles,il y'a pas mal de buggs au omnet de l'insertion apres creation sur le serveur distant. ta premiere tache sera de dianostiquer mon code et me dire qulles sont les causes possibles : ": Véhicule distant créé avec ID: 6b7c1f4a-b1a7-46b7-ba27-a284548a3e30
2026-01-31T12:53:50.866+01:00 ERROR 170120 --- [fleet-management] [tor-tcp-epoll-1] a.w.r.e.AbstractErrorWebExceptionHandler : [5d50d22a-2]  500 Server Error for HTTP POST "/api/v1/vehicles"

org.springframework.dao.DataIntegrityViolationException: executeMany; SQL [INSERT INTO fleet.vehicles (id, manager_id, vehicle_type_id, license_plate, brand, model, status, photo_url) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)]; insert or update on table "vehicles" violates foreign key constraint "vehicles_vehicle_type_id_fkey"
	at org.springframework.r2dbc.connection.ConnectionFactoryUtils.convertR2dbcException(ConnectionFactoryUtils.java:247) ~[spring-r2dbc-6.1.8.jar:6.1.8]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ Handler com.yowyob.fleet.infrastructure.adapters.inbound.rest.VehicleController#createIndependent(VehicleRegistrationRequest, Authentication, String) [DispatcherHandler]
	*__checkpoint ⇢ AuthorizationWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ ExceptionTranslationWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ LogoutWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ ServerRequestCacheWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ SecurityContextServerWebExchangeWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ AuthenticationWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ ReactorContextWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ HttpHeaderWriterWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ ServerWebExchangeReactorContextWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.WebFilterChainProxy [DefaultWebFilterChain]
	*__checkpoint ⇢ HTTP POST "/api/v1/vehicles" [ExceptionHandlingWebHandler]
Original Stack Trace:
		at org.springframework.r2dbc.connection.ConnectionFactoryUtils.convertR2dbcException(ConnectionFactoryUtils.java:247) ~[spring-r2dbc-6.1.8.jar:6.1.8]
		at org.springframework.r2dbc.core.DefaultDatabaseClient.lambda$inConnectionMany$8(DefaultDatabaseClient.java:156) ~[spring-r2dbc-6.1.8.jar:6.1.8]
		at reactor.core.publisher.Flux.lambda$onErrorMap$28(Flux.java:7302) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.Flux.lambda$onErrorResume$29(Flux.java:7355) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxOnErrorResume$ResumeSubscriber.onError(FluxOnErrorResume.java:94) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxUsingWhen$UsingWhenSubscriber.deferredError(FluxUsingWhen.java:403) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxUsingWhen$RollbackInner.onComplete(FluxUsingWhen.java:480) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.onComplete(Operators.java:2231) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.MonoIgnoreElements$IgnoreElementsSubscriber.onComplete(MonoIgnoreElements.java:89) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onComplete(FluxFilter.java:166) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFilter$FilterConditionalSubscriber.onComplete(FluxFilter.java:300) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onComplete(FluxMap.java:275) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2573) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxMap$MapConditionalSubscriber.request(FluxMap.java:295) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFilter$FilterConditionalSubscriber.request(FluxFilter.java:321) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFilter$FilterSubscriber.request(FluxFilter.java:186) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxMap$MapSubscriber.request(FluxMap.java:164) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.MonoIgnoreElements$IgnoreElementsSubscriber.onSubscribe(MonoIgnoreElements.java:72) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxMap$MapSubscriber.onSubscribe(FluxMap.java:92) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onSubscribe(FluxFilter.java:85) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFilter$FilterConditionalSubscriber.onSubscribe(FluxFilter.java:219) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onSubscribe(FluxMap.java:194) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.MonoJust.subscribe(MonoJust.java:55) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.MonoDeferContextual.subscribe(MonoDeferContextual.java:55) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:76) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:53) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.Mono.subscribe(Mono.java:4568) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxUsingWhen$UsingWhenSubscriber.onError(FluxUsingWhen.java:368) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.checkTerminated(FluxFlatMap.java:846) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.drainLoop(FluxFlatMap.java:612) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.drain(FluxFlatMap.java:592) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.innerError(FluxFlatMap.java:867) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFlatMap$FlatMapInner.onError(FluxFlatMap.java:994) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxHandle$HandleSubscriber.onError(FluxHandle.java:213) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.MonoFlatMapMany$FlatMapManyInner.onError(MonoFlatMapMany.java:256) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxHandleFuseable$HandleFuseableSubscriber.onNext(FluxHandleFuseable.java:201) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableConditionalSubscriber.onNext(FluxFilterFuseable.java:337) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onNext(FluxContextWrite.java:107) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onNext(FluxPeekFuseable.java:854) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onNext(FluxPeekFuseable.java:854) ~[reactor-core-3.6.6.jar:3.6.6]
		at io.r2dbc.postgresql.util.FluxDiscardOnCancel$FluxDiscardOnCancelSubscriber.onNext(FluxDiscardOnCancel.java:91) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
		at reactor.core.publisher.FluxDoFinally$DoFinallySubscriber.onNext(FluxDoFinally.java:113) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxHandle$HandleSubscriber.onNext(FluxHandle.java:129) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxCreate$BufferAsyncSink.drain(FluxCreate.java:880) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxCreate$BufferAsyncSink.next(FluxCreate.java:805) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxCreate$SerializedFluxSink.next(FluxCreate.java:163) ~[reactor-core-3.6.6.jar:3.6.6]
		at io.r2dbc.postgresql.client.ReactorNettyClient$Conversation.emit(ReactorNettyClient.java:684) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
		at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.emit(ReactorNettyClient.java:936) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
		at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.onNext(ReactorNettyClient.java:810) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
		at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.onNext(ReactorNettyClient.java:716) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
		at reactor.core.publisher.FluxHandle$HandleSubscriber.onNext(FluxHandle.java:129) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onNext(FluxPeekFuseable.java:854) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onNext(FluxMap.java:224) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onNext(FluxMap.java:224) ~[reactor-core-3.6.6.jar:3.6.6]
		at reactor.netty.channel.FluxReceive.drainReceiver(FluxReceive.java:294) ~[reactor-netty-core-1.1.19.jar:1.1.19]
		at reactor.netty.channel.FluxReceive.onInboundNext(FluxReceive.java:403) ~[reactor-netty-core-1.1.19.jar:1.1.19]
		at reactor.netty.channel.ChannelOperations.onInboundNext(ChannelOperations.java:426) ~[reactor-netty-core-1.1.19.jar:1.1.19]
		at reactor.netty.channel.ChannelOperationsHandler.channelRead(ChannelOperationsHandler.java:114) ~[reactor-netty-core-1.1.19.jar:1.1.19]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346) ~[netty-codec-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318) ~[netty-codec-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1407) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:918) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:799) ~[netty-transport-classes-epoll-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:501) ~[netty-transport-classes-epoll-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:399) ~[netty-transport-classes-epoll-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[netty-common-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.110.Final.jar:4.1.110.Final]
		at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.110.Final.jar:4.1.110.Final]
		at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: io.r2dbc.postgresql.ExceptionFactory$PostgresqlDataIntegrityViolationException: insert or update on table "vehicles" violates foreign key constraint "vehicles_vehicle_type_id_fkey"
	at io.r2dbc.postgresql.ExceptionFactory.createException(ExceptionFactory.java:102) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
	at io.r2dbc.postgresql.ExceptionFactory.createException(ExceptionFactory.java:65) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
	at io.r2dbc.postgresql.ExceptionFactory.handleErrorResponse(ExceptionFactory.java:132) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
	at reactor.core.publisher.FluxHandleFuseable$HandleFuseableSubscriber.onNext(FluxHandleFuseable.java:179) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxFilterFuseable$FilterFuseableConditionalSubscriber.onNext(FluxFilterFuseable.java:337) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onNext(FluxContextWrite.java:107) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onNext(FluxPeekFuseable.java:854) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onNext(FluxPeekFuseable.java:854) ~[reactor-core-3.6.6.jar:3.6.6]
	at io.r2dbc.postgresql.util.FluxDiscardOnCancel$FluxDiscardOnCancelSubscriber.onNext(FluxDiscardOnCancel.java:91) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
	at reactor.core.publisher.FluxDoFinally$DoFinallySubscriber.onNext(FluxDoFinally.java:113) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxHandle$HandleSubscriber.onNext(FluxHandle.java:129) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxCreate$BufferAsyncSink.drain(FluxCreate.java:880) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxCreate$BufferAsyncSink.next(FluxCreate.java:805) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxCreate$SerializedFluxSink.next(FluxCreate.java:163) ~[reactor-core-3.6.6.jar:3.6.6]
	at io.r2dbc.postgresql.client.ReactorNettyClient$Conversation.emit(ReactorNettyClient.java:684) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
	at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.emit(ReactorNettyClient.java:936) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
	at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.onNext(ReactorNettyClient.java:810) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
	at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.onNext(ReactorNettyClient.java:716) ~[r2dbc-postgresql-1.0.5.RELEASE.jar:1.0.5.RELEASE]
	at reactor.core.publisher.FluxHandle$HandleSubscriber.onNext(FluxHandle.java:129) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onNext(FluxPeekFuseable.java:854) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onNext(FluxMap.java:224) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onNext(FluxMap.java:224) ~[reactor-core-3.6.6.jar:3.6.6]
	at reactor.netty.channel.FluxReceive.drainReceiver(FluxReceive.java:294) ~[reactor-netty-core-1.1.19.jar:1.1.19]
	at reactor.netty.channel.FluxReceive.onInboundNext(FluxReceive.java:403) ~[reactor-netty-core-1.1.19.jar:1.1.19]
	at reactor.netty.channel.ChannelOperations.onInboundNext(ChannelOperations.java:426) ~[reactor-netty-core-1.1.19.jar:1.1.19]
	at reactor.netty.channel.ChannelOperationsHandler.channelRead(ChannelOperationsHandler.java:114) ~[reactor-netty-core-1.1.19.jar:1.1.19]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346) ~[netty-codec-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318) ~[netty-codec-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1407) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:918) ~[netty-transport-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:799) ~[netty-transport-classes-epoll-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:501) ~[netty-transport-classes-epoll-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:399) ~[netty-transport-classes-epoll-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[netty-common-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.110.Final.jar:4.1.110.Final]
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.110.Final.jar:4.1.110.Final]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

^C[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  03:36 min
[INFO] Finished at: 2026-01-31T12:54:13+01:00
[INFO] ------------------------------------------------------------------------
gabriel@gabriel-pc:~/Documents/projects/ecole/5GI/projet-synthese/fleet-management/code$ 

"