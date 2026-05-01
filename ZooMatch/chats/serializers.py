from rest_framework import serializers
from django.contrib.auth import get_user_model
from django.db.models import Q

from .models import Message

from users.serializers import SimpleUserSerializer

from matching.models import Match


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
        read_only_fields = ('id', 'is_read')

    def validate(self, data):
        sender = self.context.get('request').user
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
