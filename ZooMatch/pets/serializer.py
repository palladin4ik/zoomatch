from rest_framework import serializers
from drf_spectacular.utils import extend_schema_field

from .models import (AnimalType, Breed, Tag, Pet, PetInfo, Comment,
                     Match)
from users.serializers import UserSerializer, Base64FileField


class AnimalTypeSerializer(serializers.ModelSerializer):

    class Meta:
        model = AnimalType
        fields = ('id', 'name')


class BreedSerializer(serializers.ModelSerializer):
    animal_type = AnimalTypeSerializer(read_only=True)
    animal_type_id = serializers.PrimaryKeyRelatedField(
        queryset=AnimalType.objects.all(), write_only=True
    )

    class Meta:
        model = Breed
        fields = ('id', 'name', 'animal_type_id', 'animal_type')
        read_only_fields = ('id',)


class TagSerializer(serializers.ModelSerializer):

    class Meta:
        model = Tag
        fields = ('id', 'tag')
        read_only_fields = ('id',)


class PetSerializer(serializers.ModelSerializer):
    owner = UserSerializer(read_only=True)
    animal_type = AnimalTypeSerializer(read_only=True)
    breed = BreedSerializer(read_only=True)
    tags = serializers.SerializerMethodField()

    avatar = Base64FileField(read_only=True)
    pedigree_documents = serializers.CharField(read_only=True)

    class Meta:
        model = Pet
        fields = ('id', 'name', 'animal_type', 'breed', 'is_male',
                  'age', 'owner', 'avatar', 'location', 'has_pedigree',
                  'pedigree_documents', 'awards', 'tags', 'description',
                  'is_active')
        read_only_fields = ('id',)

    @extend_schema_field(TagSerializer(many=True))
    def get_tags(self, obj):
        return TagSerializer(obj.tags.all(), many=True).data


class PetCreateUpdateSerializer(serializers.ModelSerializer):
    tags = serializers.ListField(
        child=serializers.CharField(max_length=50),
        required=False,
        write_only=True
    )
    tags_list = serializers.SerializerMethodField(read_only=True)

    avatar = Base64FileField(required=False, allow_null=True, allow_blank=True)
    pedigree_documents = Base64FileField(required=False, allow_null=True,
                                         allow_blank=True)

    class Meta:
        model = Pet
        fields = ('id', 'name', 'animal_type', 'breed', 'is_male',
                  'age', 'avatar', 'location', 'has_pedigree',
                  'pedigree_documents', 'awards', 'tags', 'tags_list',
                  'description', 'is_active')
        read_only_fields = ('id',)

    @extend_schema_field(TagSerializer(many=True))
    def get_tags_list(self, obj):
        return TagSerializer(obj.tags.all(), many=True).data

    def validate_tags(self, values):
        cleaned = []
        for value in values:
            tag_name = value.strip().lower()
            if tag_name not in cleaned:
                cleaned.append(tag_name)
        return cleaned

    def create(self, validated_data):
        tags_data = validated_data.pop('tags', [])

        breed = validated_data.get('breed')
        if breed:
            validated_data['animal_type'] = breed.animal_type

        pet = Pet.objects.create(**validated_data)

        for tag_name in tags_data:
            tag, _ = Tag.objects.get_or_create(tag=tag_name)
            pet.tags.add(tag)

        return pet

    def update(self, instance, validated_data):
        tags_data = validated_data.pop('tags', None)

        breed = validated_data.get('breed')
        if breed:
            validated_data['animal_type'] = breed.animal_type
        for attr, value in validated_data.items():
            setattr(instance, attr, value)
        instance.save()

        if tags_data is not None:
            instance.tags.clear()
            for tag_name in tags_data:
                tag, _ = Tag.objects.get_or_create(tag=tag_name)
                instance.tags.add(tag)

        return instance


class MatchSerializer(serializers.ModelSerializer):

    class Meta:
        model = Match
        fields = ('id', 'pet_from', 'pet_to', 'created_at')
        read_only_fields = ('id', 'created_at')


class PetInfoSerializer(serializers.ModelSerializer):
    pet_id = serializers.PrimaryKeyRelatedField(
        queryset=Pet.objects.all(), source='pet'
    )

    class Meta:
        model = PetInfo
        fields = ('id', 'pet_id', 'watchers', 'likes')
        read_only_fields = ('id', 'pet_id', 'watchers', 'likes')


class CommentSerializer(serializers.ModelSerializer):
    author = UserSerializer()
    pet_info_card_id = serializers.PrimaryKeyRelatedField(
        queryset=PetInfo.objects.all(), source='pet_info_card'
    )

    class Meta:
        model = Comment
        fields = ('id', 'content', 'likes', 'author', 'pet_info_card_id',
                  'date_create')
        read_only_fields = ('id', 'likes', 'author', 'date_create')

    def create(self, validated_data):
        validated_data['author'] = self.context['request'].user
        return super().create(validated_data)
