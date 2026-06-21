import os

from rest_framework import mixins, permissions, viewsets, status
from rest_framework.response import Response
from rest_framework.decorators import action
from rest_framework.views import APIView
from rest_framework.parsers import MultiPartParser
from django.db.models import Q, OuterRef, Subquery, Count
from django.contrib.auth import get_user_model
from django.conf import settings
from django.http import FileResponse

from drf_spectacular.utils import (extend_schema, extend_schema_view,
                                   OpenApiParameter, OpenApiTypes,
                                   OpenApiResponse)

from .serializers import (MessageSerializer, ChatSerializer,
                          MessageMediaSerializer)
from .models import Message
from .pagination import MessagePagination
from .media_utils import get_content_type

from matching.models import Match


User = get_user_model()


@extend_schema_view(
    list=extend_schema(
        summary='Список всех сообщений пользователя',
        description='Возвращает список всех сообщений пользователя, '
                    'отправившего запрос, где пользователь или отправитель, '
                    'или получатель.\n\nСообщения фильтруются по дате '
                    'отправления.',
        parameters=[
            OpenApiParameter(
                name='receiver',
                description='ID собеседника',
                required=True,
                type=OpenApiTypes.INT,
            )
        ]
    ),
    create=extend_schema(
        summary='Отправить сообщение',
        description='Создает новое сообщение, отправитель - пользователь, '
                    'отправивший запрос.\n\nОтправить сообщение можно только '
                    'при наличии метча у обоих пользователей.'
    ),
    update=extend_schema(
        summary='Заглушка, не использовать, вернет 405 ;)'
    ),
    partial_update=extend_schema(
        summary='Отредактировать сообщение',
        description='Отредактировать сообщение, редактирование доступно '
                    'только отправителю'
    ),
    destroy=extend_schema(
        summary='Удалить сообщение для себя',
        description='Удалить сообщение для себя (soft delete). '
    )
)
class MessageViewSet(mixins.CreateModelMixin, mixins.ListModelMixin,
                     mixins.UpdateModelMixin, mixins.DestroyModelMixin,
                     viewsets.GenericViewSet):
    queryset = Message.objects.all()
    serializer_class = MessageSerializer
    permission_classes = [permissions.IsAuthenticated]
    pagination_class = MessagePagination
    http_method_names = ['get', 'post', 'patch', 'delete']

    def perform_create(self, serializer):
        serializer.save(sender=self.request.user)

    def get_queryset(self):
        user_1 = self.request.user

        if self.action == 'list':
            user_2 = self.request.query_params.get('receiver')

            if not user_2:
                return Message.objects.none()

            return Message.objects.filter(
                Q(sender=user_1, receiver=user_2, deleted_by_sender=False) |
                Q(sender=user_2, receiver=user_1, deleted_by_receiver=False)
            )

        return Message.objects.filter(
            Q(sender=user_1) | Q(receiver=user_1)
        )

    def destroy(self, request, pk=None):
        user = self.request.user
        message = self.get_object()

        if message.sender == user:
            message.deleted_by_sender = True
        elif message.receiver == user:
            message.deleted_by_receiver = True
        else:
            return Response(status=status.HTTP_403_FORBIDDEN)

        message.save()

        return Response(status=status.HTTP_204_NO_CONTENT)

    def partial_update(self, request, pk=None):
        user = self.request.user
        message = self.get_object()

        if message.sender != user:
            return Response(status=status.HTTP_403_FORBIDDEN)

        serializer = self.get_serializer(message,
                                         data=request.data, partial=True)
        # Двойная проверка валидности (делить сериализаторы)
        serializer.is_valid(raise_exception=True)
        serializer.save()

        return Response(serializer.data, status=status.HTTP_200_OK)

    @extend_schema(
            summary='Удалить сообщение для всех',
            description='Удаляет сообщение для всех, удалить может '
                        'только отправитель.',
            request=None,
            responses=None
    )
    @action(detail=True, methods=['post'])
    def delete_for_all(self, request, pk=None):
        user = self.request.user
        message = self.get_object()

        if message.sender != user:
            return Response(status=status.HTTP_403_FORBIDDEN)

        message.deleted_by_sender = True
        message.deleted_by_receiver = True
        message.save()

        return Response(status=status.HTTP_204_NO_CONTENT)

    @extend_schema(
            summary='Отметить сообщение прочитаным',
            description='Отмечает сообщение прочитаным, '
                        'отмечается только получателем',
            request=None,
            responses={
                200: OpenApiResponse(
                    response={
                        'type': 'object',
                        'properties': {
                            'detail': {
                                'type': 'string',
                                'example': 'Сообщение прочитано'
                            }
                        }
                    }
                )
            }
    )
    @action(detail=True, methods=['post'])
    def read(self, request, pk=None):
        user = self.request.user
        message = self.get_object()

        if message.receiver != user:
            return Response(status=status.HTTP_403_FORBIDDEN)

        if not message.is_read:
            message.is_read = True
            message.save()

        return Response({'detail': 'Сообщение прочитано'},
                        status=status.HTTP_200_OK)


class MessageMediaView(APIView):
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [MultiPartParser]

    @extend_schema(
            summary='Получение медиа сообщения',
            description='Возвращает медиа файл, проверяя является ли '
            'пользователь, отправивший запрос получателем или отправителем',
            responses={
                200: OpenApiResponse(
                    description='Файл',
                    response={
                        'type': 'string',
                        'format': 'binary'
                    }
                ),
            }
    )
    def get(self, request, pk):
        try:
            message = Message.objects.get(pk=pk)
        except Message.DoesNotExist:
            return Response(
                {'detail': 'Сообщение не найдено'},
                status=status.HTTP_404_NOT_FOUND
            )

        if request.user not in (message.sender, message.receiver):
            return Response(
                {'detail': 'Нет доступа'},
                status=status.HTTP_403_FORBIDDEN
            )

        if not message.media:
            return Response(
                {'detail': 'Медиа не прикреплено'},
                status=status.HTTP_404_NOT_FOUND
            )

        file_path = os.path.join(settings.MEDIA_ROOT, message.media.name)
        if not os.path.exists(file_path):
            return Response(
                {'detail': 'Файл не найден'},
                status=status.HTTP_404_NOT_FOUND
            )

        content_type = get_content_type(message.media.name)

        return FileResponse(
            open(file_path, 'rb'),
            content_type=content_type,
            as_attachment=False
        )

    @extend_schema(
        summary='Прикрепить медиа к сообщению',
        request={
            'multipart/form-data': {
                'type': 'object',
                'properties': {
                    'media': {
                        'type': 'string',
                        'format': 'binary'
                    }
                }
            }
        },
        responses={
            201: MessageMediaSerializer
        }
    )
    def post(self, request, pk):
        try:
            message = Message.objects.get(pk=pk)
        except Message.DoesNotExist:
            return Response(
                {'detail': 'Сообщение не найдено'},
                status=status.HTTP_404_NOT_FOUND
            )

        if message.sender != request.user:
            return Response(
                {'detail': 'Нет доступа'},
                status=status.HTTP_403_FORBIDDEN
            )

        if message.media:
            return Response(
                {'detail': 'Медиа уже прикреплено'},
                status=status.HTTP_400_BAD_REQUEST
            )

        serializer = MessageMediaSerializer(message, data=request.data,
                                            partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()

        if message.text == '[Файл]':
            message.text = None
            message.save(update_fields=['text'])

        return Response(serializer.data, status=status.HTTP_201_CREATED)


@extend_schema_view(
    list=extend_schema(
        summary='Список чатов пользователя',
        description='Возвращает список чатов пользователя, '
                    'отправившего запрос с последним сообщением '
                    'и количеством непрочитанных'
    )
)
class ChatViewSet(mixins.ListModelMixin, viewsets.GenericViewSet):
    serializer_class = ChatSerializer
    permission_classes = [permissions.IsAuthenticated]

    def list(self, request):
        user = request.user

        last_message_qs = Message.objects.filter(
            Q(sender=user, receiver=OuterRef('pk')) |
            Q(sender=OuterRef('pk'), receiver=user)
        ).order_by('-created_at').values('text')[:1]

        unread_qs = Message.objects.filter(
            sender=OuterRef('pk'),
            receiver=user,
            is_read=False,
            deleted_by_receiver=False,
            deleted_by_sender=False
        ).values('sender').annotate(c=Count('id')).values('c')

        interlocutors = User.objects.filter(
            Q(
                pets__sent_matches__pet_to__owner=user,
                pets__sent_matches__status=Match.Status.ACCEPTED
            ) |
            Q(
                pets__received_matches__pet_from__owner=user,
                pets__received_matches__status=Match.Status.ACCEPTED
            )
        ).distinct().annotate(
            last_message_text=Subquery(last_message_qs),
            unread_count=Subquery(unread_qs)
        )

        serializer = self.get_serializer(interlocutors, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)
