from rest_framework import serializers

from .models import Match, Rejection, ActionCategory, Action
from pets.serializers import PetSerializer
from pets.models import Pet


class MatchSerializer(serializers.ModelSerializer):
    pet_from = serializers.PrimaryKeyRelatedField(queryset=Pet.objects.all())
    pet_to = serializers.PrimaryKeyRelatedField(queryset=Pet.objects.all())

    pet_from_data = PetSerializer(source='pet_from', read_only=True)
    pet_to_data = PetSerializer(source='pet_to', read_only=True)

    class Meta:
        model = Match
        fields = ('id', 'pet_from', 'pet_to', 'status', 'created_at',
                  'pet_from_data', 'pet_to_data')
        read_only_fields = ('id', 'created_at')


class RejectionSerializer(serializers.ModelSerializer):
    pet_from = serializers.PrimaryKeyRelatedField(queryset=Pet.objects.all())
    pet_to = serializers.PrimaryKeyRelatedField(queryset=Pet.objects.all())

    class Meta:
        model = Rejection
        fields = ('id', 'pet_from', 'pet_to', 'count', 'last_rejected_at')
        read_only_fields = ('id', 'last_rejected_at')


class ActionCategorySerializer(serializers.ModelSerializer):

    class Meta:
        model = ActionCategory
        fields = ('id', 'name')
        read_only_fields = ('id',)


class ActionSerializer(serializers.ModelSerializer):
    category = serializers.PrimaryKeyRelatedField(
        queryset=ActionCategory.objects.all()
        )

    pet = serializers.PrimaryKeyRelatedField(queryset=Pet.objects.all())

    class Meta:
        model = Action
        fields = ('id', 'pet', 'category', 'created_at')
        read_only_fields = ('id', 'created_at')
