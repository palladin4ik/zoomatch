from rest_framework import mixins, permissions, viewsets, status
from rest_framework.response import Response
from rest_framework.decorators import action
from django.db.models import Q

from .serializers import MessageSerializer
from .models import Message


class MessageViewSet(mixins.CreateModelMixin, mixins.ListModelMixin,
                     mixins.UpdateModelMixin, mixins.DestroyModelMixin,
                     viewsets.GenericViewSet):
    serializer_class = MessageSerializer
    permission_classes = [permissions.IsAuthenticated]

    def perform_create(self, serializer):
        serializer.save(sender=self.request.user)

    def get_queryset(self):
        user_1 = self.request.user
        user_2 = self.request.query_params.get('receiver')

        if not user_2:
            return Message.objects.none()

        qs = Message.objects.filter(
            Q(sender=user_1, receiver=user_2, deleted_by_sender=False) |
            Q(sender=user_2, receiver=user_1, deleted_by_receiver=False)
        )

        return qs

    def destroy(self, request, *args, **kwargs):
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

    @action(detail=True, methods=['post'])
    def delete_for_all(self, request, *args, **kwargs):
        user = self.request.user
        message = self.get_object()

        if message.sender != user:
            return Response(status=status.HTTP_403_FORBIDDEN)

        message.deleted_by_sender = True
        message.deleted_by_receiver = True
        message.save()

        return Response(status=status.HTTP_204_NO_CONTENT)

    def partial_update(self, request, *args, **kwargs):
        user = self.request.user
        message = self.get_object()

        if message.sender != user:
            return Response(status=status.HTTP_403_FORBIDDEN)
        
        serializer = self.get_serializer(message, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)  # Двойная проверка валидности (делить сериализаторы)
        serializer.save()
        
        return Response(serializer.data, status=status.HTTP_200_OK)
    
    def update(self, request, *args, **kwargs):
        return Response(status=status.HTTP_405_METHOD_NOT_ALLOWED)
    
    @action(detail=True, methods=['post'])
    def read(self, request, *args, **kwargs):
        user = self.request.user
        message = self.get_object()

        if message.receiver != user:
            return Response(status=status.HTTP_403_FORBIDDEN)

        if not message.is_read:
            message.is_read = True
            message.save()
        
        return Response({"status": "message read"}, status=status.HTTP_200_OK)
