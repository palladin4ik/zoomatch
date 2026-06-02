from rest_framework import serializers
from django.contrib.auth import get_user_model
from django.db.models import Q

from .models import Message

from users.serializers import SimpleUserSerializer

from matching.models import Match

from core.validators import validate_file_size, validate_media_type


User = get_user_model()


class MessageSerializer(serializers.ModelSerializer):
    sender = SimpleUserSerializer(read_only=True, allow_null=True)
    receiver = SimpleUserSerializer(read_only=True, allow_null=True)

    receiver_id = serializers.PrimaryKeyRelatedField(
        queryset=User.objects.all(),
        source='receiver',
        write_only=True
    )

    class Meta:
        model = Message
        fields = ('id', 'sender', 'receiver', 'text', 'media', 'is_read',
                  'receiver_id', 'created_at')
        read_only_fields = ('id', 'media', 'is_read')

    def validate(self, data):
        request = self.context.get('request')

        if request.method != 'POST':
            return data

        sender = request.user
        receiver = data.get('receiver')

        is_matched = Match.objects.filter(
            Q(pet_from__owner=sender, pet_to__owner=receiver) |
            Q(pet_to__owner=sender, pet_from__owner=receiver),
            status=Match.Status.ACCEPTED
            )

        if is_matched.exists():
            return data
        else:
            raise serializers.ValidationError("Для начала общения нужен Match")


class MessageMediaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Message
        fields = ['media']

    def validate_media(self, value):
        validate_file_size(value)
        validate_media_type(value)
        return value


class ChatSerializer(serializers.Serializer):
    user = SimpleUserSerializer(source='*', read_only=True)
    last_message_text = serializers.CharField(read_only=True, allow_null=True)
    unread_count = serializers.IntegerField(read_only=True, allow_null=True)
