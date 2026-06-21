from rest_framework import viewsets, permissions, mixins, status
from rest_framework.response import Response
from django.db.models import Q, F

from drf_spectacular.utils import (extend_schema, extend_schema_view,
                                   OpenApiParameter, OpenApiTypes)

from .serializers import MatchSerializer
from .models import Match, Rejection, Action, ActionCategory
from chats.models import Message


def send_match_welcome_messages(match):
    pet_from = match.pet_from
    pet_to = match.pet_to

    owner_from = pet_from.owner
    owner_to = pet_to.owner

    existing = Message.objects.filter(
        Q(sender=owner_from, receiver=owner_to) |
        Q(sender=owner_to, receiver=owner_from)
    ).exists()

    if existing:
        return

    text_from = f'Мэтч! {pet_from.name} и {pet_to.name} понравились друг '
    'другу 🐾 Начните общение!'
    text_to = f'Мэтч! {pet_to.name} и {pet_from.name} понравились друг другу '
    '🐾 Начните общение!'

    Message.objects.create(
        sender=owner_from,
        receiver=owner_to,
        text=text_from,
    )
    Message.objects.create(
        sender=owner_to,
        receiver=owner_from,
        text=text_to,
    )


@extend_schema_view(
    list=extend_schema(
        summary='Список метчей пользователя',
        description='Возвращает список всех метчей пользователя, отправившего '
                    'запрос. Принимает параметр match_type = sent (Список '
                    'отправленных метчей) и match_type = received (Список '
                    'полученных метчей), при отсутствующем параметре '
                    'возвращает список всех метчей, напомни мне добавить '
                    'фильтр по дате, я забыл',
        parameters=[
            OpenApiParameter(
                name='match_type',
                description='Тип метчей (отправленные/полученные, при '
                            'отсутсвии - все)',
                required=False,
                type=OpenApiTypes.STR,
                enum=['sent', 'received']
            )
        ]
    ),
    create=extend_schema(
        summary='Создать метч',
        description='Создает метч с дефолтным статусом 0 (ожидание), если метч'
                    ' уже создан (pet_1 -> pet_2) и создается обратный '
                    '(pet_2 -> pet_1), первый (pet_1 -> pet_2) автоматически '
                    'становится принятым (Accepted)(pet_2->pet_1 не '
                    'создается)',
        request={
            'application/json': {
                'type': 'object',
                'properties': {
                    'pet_from': {
                        'type': 'number',
                        'example': 3,
                    },
                    'pet_to': {
                        'type': 'number',
                        'example': 6
                    },
                },
                'required': ['pet_from', 'pet_to'],
            }
        },
    ),
    partial_update=extend_schema(
        summary='Изменить статус метча',
        description='Изменить статус метча (1 - Accepted / 2 - Rejected)\n\n'
                    'Также при отказе инкрементируется запись в таблице '
                    'Rejection'
    )
)
class MatchViewSet(mixins.CreateModelMixin, mixins.ListModelMixin,
                   mixins.UpdateModelMixin, viewsets.GenericViewSet):
    serializer_class = MatchSerializer
    permission_classes = [permissions.IsAuthenticated]
    queryset = Match.objects.all()
    pagination_class = None
    http_method_names = ['get', 'post', 'patch']

    def perform_create(self, serializer):
        pet_from = serializer.validated_data['pet_from']
        pet_to = serializer.validated_data['pet_to']

        reverse_match = Match.objects.filter(pet_from=pet_to,
                                             pet_to=pet_from).first()

        like_category = ActionCategory.objects.get(name='like')
        Action.objects.create(user=pet_from.owner, pet=pet_to,
                              category=like_category)

        if reverse_match:
            reverse_match.status = Match.Status.ACCEPTED
            reverse_match.save()

            send_match_welcome_messages(reverse_match)

            return

        serializer.save()

    def get_queryset(self):
        user = self.request.user
        match_type = self.request.query_params.get('type')

        if match_type == 'sent':
            qs = Match.objects.filter(
                pet_from__owner=user
            )
        elif match_type == 'received':
            qs = Match.objects.filter(
                pet_to__owner=user
            )
        else:
            qs = Match.objects.filter(
                Q(pet_from__owner=user) |
                Q(pet_to__owner=user)
            )

        return qs.select_related('pet_from', 'pet_to')

    def partial_update(self, request, pk=None):
        match = self.get_object()

        if match.pet_to.owner != request.user:
            return Response(status=status.HTTP_403_FORBIDDEN)

        new_status = request.data.get('status')

        if match.status == new_status:
            return Response(
                {'detail': 'Статус уже установлен'},
                status=status.HTTP_400_BAD_REQUEST
            )

        if match.status == new_status:
            return Response(
                {'detail': 'Статус уже установлен'},
                status=status.HTTP_400_BAD_REQUEST
            )

        if new_status == Match.Status.REJECTED:
            pet_from = match.pet_from
            pet_to = match.pet_to

            rejection, created = Rejection.objects.get_or_create(
                pet_from=pet_from,
                pet_to=pet_to
            )

            if not created:
                rejection.count = F('count') + 1
                rejection.save()

        serializer = self.get_serializer(match, data={'status': new_status},
                                         partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()

        if new_status == Match.Status.ACCEPTED:
            send_match_welcome_messages(match)

        return Response(serializer.data, status=status.HTTP_200_OK)
