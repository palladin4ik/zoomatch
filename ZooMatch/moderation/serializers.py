from rest_framework import serializers

from .models import ModerationRequest

from pets.serializer import PetSerializer
from users.serializers import SimpleUserSerializer


class ModerationRequestSerializer(serializers.ModelSerializer):
    pet = PetSerializer(read_only=True)
    reviewed_by = SimpleUserSerializer(allow_null=True, read_only=True)

    class Meta:
        model = ModerationRequest
        fields = ('id', 'pet', 'animal_type', 'breed', 'status',
                  'created_at', 'reviewed_by', 'reviewed_at')
        read_only_fields = ('id', 'pet', 'animal_type', 'breed',
                            'created_at', 'reviewed_by')
