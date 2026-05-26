from rest_framework import viewsets, permissions, status
from rest_framework.response import Response
from rest_framework.decorators import action
from django.db.models import Q

from drf_spectacular.utils import (extend_schema, extend_schema_view,
                                   OpenApiParameter, OpenApiResponse,
                                   OpenApiTypes)

from .serializers import (
    AnimalTypeSerializer, BreedSerializer,
    PetSerializer, PetCreateUpdateSerializer,
    PetInfoSerializer, CommentSerializer,
)
from .models import AnimalType, Breed, Pet, PetInfo, Comment
from .permissions import IsAdminOrReadOnly, IsOwnerOrReadOnly

from matching.models import Action, ActionCategory


COMMON_PERMISSION_DESCRIPTION = '''
    Права доступа:
    - Просмотр доступен всем авторизованным пользователям
    - Создание, обновление, удаление - только администраторам (is_staff)
'''

COMMON_OWNER_PERMISSION = '''
    Права доступа:
    - Просмотр доступен всем авторизованным пользователям
    - Создание, обновление, удаление - только владельцу (owner==request.user)
'''


@extend_schema_view(
    list=extend_schema(
        summary='Список всех типов животных',
        description='Выводит список всех типов животных\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    retrieve=extend_schema(
        summary='Тип животного по ID',
        description='Возвращает конкретный тип животного по его `id`\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    create=extend_schema(
        summary='Создать новый тип животного',
        description='Добавляет новый тип животногоу\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    update=extend_schema(
        summary='Обновить тип животного',
        description='Полностью обновляет информацию о типе животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    partial_update=extend_schema(
        summary='Частично обновить тип животного',
        description='Обновляет отдельные поля типа животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    destroy=extend_schema(
        summary='Удалить тип животного',
        description='Удаляет тип животного по его `id`\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
)
class AnimalTypeViewSet(viewsets.ModelViewSet):
    queryset = AnimalType.objects.all()
    serializer_class = AnimalTypeSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdminOrReadOnly]
    pagination_class = None


@extend_schema_view(
    list=extend_schema(
        summary='Список всех пород животных',
        description='Выводит список всех пород животных\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    retrieve=extend_schema(
        summary='Порода по ID',
        description='Возвращает конкретный породу по его `id`\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    create=extend_schema(
        summary='Создать новую породу животного',
        description='Добавляет новую породу животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    update=extend_schema(
        summary='Обновить породу животного',
        description='Полностью обновляет информацию о породе животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    partial_update=extend_schema(
        summary='Частично обновить породу животного',
        description='Обновляет отдельные поля породы животного\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
    destroy=extend_schema(
        summary='Удалить породу животного',
        description='Удаляет породу животного по его `id`\n\n' +
                    COMMON_PERMISSION_DESCRIPTION,
    ),
)
class BreedViewSet(viewsets.ModelViewSet):
    queryset = Breed.objects.all().prefetch_related('animal_type')
    serializer_class = BreedSerializer
    permission_classes = [permissions.IsAuthenticated, IsAdminOrReadOnly]
    pagination_class = None


@extend_schema_view(
    list=extend_schema(
        summary='Список всех животных',
        description='Выводит список всех активных (is_active) животных, '
                    'у которых хозяин не пользователь, отправивший запрос'
                    'если с запросом передан query параметр `animal_type`, '
                    'то вывод питомцев этого типа\n\n' +
                    COMMON_OWNER_PERMISSION,
        parameters=[
            OpenApiParameter(
                name='animal_type',
                description='Фильтр по типу животного',
                required=False,
                type=int,
                location=OpenApiParameter.QUERY,
            ),
        ]
    ),
    retrieve=extend_schema(
        summary='Животное по ID',
        description='Возвращает конкретное **активное** животное, '
                    'если запросивший пользователь не его владелец, или любое '
                    'конкретное животное владельца по его `id`\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
    create=extend_schema(
        summary='Создать нового питомца',
        description='Создает нового питомца, владелец - пользователь, '
                    'отправивший запрос\n\n' + COMMON_OWNER_PERMISSION,
    ),
    update=extend_schema(
        summary='Обновить питомца',
        description='Полностью обновляет информацию о питомце\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
    partial_update=extend_schema(
        summary='Частично обновить питомца',
        description='Обновляет отдельные поля питомца\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
    destroy=extend_schema(
        summary='Удалить питомца',
        description='Удаляет питомца по его `id`\n\n' +
                    COMMON_OWNER_PERMISSION,
    ),
)
class PetViewSet(viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        user = self.request.user
        animal_type = self.request.query_params.get('animal_type')

        if self.action in ['update', 'partial_update', 'destroy']:
            qs = Pet.objects.filter(owner=user)

        elif self.action in ['list', 'view']:
            qs = Pet.objects.filter(is_active=True).exclude(owner=user)

            if animal_type:
                qs = qs.filter(animal_type=animal_type)

        elif self.action == 'retrieve':
            qs = Pet.objects.filter(Q(is_active=True) | Q(owner=user))

        else:
            qs = Pet.objects.none()

        qs = qs.select_related('owner', 'breed', 'animal_type')

        qs = qs.prefetch_related('tags')

        return qs

    def get_serializer_class(self):
        if self.action in ['create', 'update', 'partial_update']:
            return PetCreateUpdateSerializer
        return PetSerializer

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)

    @extend_schema(
            summary='Получить всех питомцев пользователя',
            description='Возвращает всех питомцев владельца, '
                        'отправившего запрос',
            parameters=[
                OpenApiParameter(
                    name='limit',
                    description='Number of results to return per page.',
                    required=False,
                    type=OpenApiTypes.INT,
                ),
                OpenApiParameter(
                    name='offset',
                    description='The initial index from which to return the '
                                'results.',
                    required=False,
                    type=OpenApiTypes.INT,
                )
                ],
            responses=OpenApiResponse(PetSerializer(many=True)),
    )
    @action(detail=False, methods=['get'])
    def me(self, request):
        queryset = Pet.objects.filter(owner=request.user).select_related(
            'owner', 'breed', 'animal_type'
        ).prefetch_related('tags')

        page = self.paginate_queryset(queryset)
        if page is not None:
            serializer = PetSerializer(page, many=True)
            return self.get_paginated_response(serializer.data)

        serializer = PetSerializer(queryset, many=True)
        return Response(serializer.data)

    @action(detail=True, methods=['post'])
    def view(self, request, pk=None):
        user = self.request.user
        pet = self.get_object()
        duration = request.data.get('duration')

        if not duration:
            return Response(
                {'detail': 'Длительность в секундах обязательный параметр'},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            duration = int(duration)
        except (ValueError, TypeError):
            return Response(
                {'detail': 'Длительность должна быть числом'},
                status=status.HTTP_400_BAD_REQUEST
            )

        view_category = ActionCategory.objects.get(name='view')
        Action.objects.create(user=user, pet=pet,
                              value=duration, category=view_category)

        return Response(status=status.HTTP_200_OK)
